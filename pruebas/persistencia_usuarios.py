"""Prueba el reinicio de la API y la lectura del formato anterior en carpetas temporales."""
import json
import os
import socket
import subprocess
import tempfile
import time
from contextlib import contextmanager
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

PROYECTO = Path(__file__).resolve().parents[1]


def pedir(url, ruta, esperado, datos=None, token=None):
    cabeceras = {"Accept": "application/json"}
    cuerpo = None
    if datos is not None:
        cuerpo = json.dumps(datos).encode("utf-8")
        cabeceras["Content-Type"] = "application/json"
    if token:
        cabeceras["Authorization"] = "Bearer " + token
    solicitud = Request(url + ruta, data=cuerpo, headers=cabeceras)
    try:
        respuesta = urlopen(solicitud, timeout=10)
    except HTTPError as error:
        respuesta = error
    with respuesta:
        contenido = respuesta.read().decode("utf-8")
        assert respuesta.status == esperado, (
            f"{ruta}: esperado {esperado}, recibido {respuesta.status}: {contenido}"
        )
        return json.loads(contenido)


@contextmanager
def servidor(carpeta, classpath, clave_admin):
    with socket.socket() as reserva:
        reserva.bind(("127.0.0.1", 0))
        puerto = reserva.getsockname()[1]
    url = f"http://127.0.0.1:{puerto}"
    entorno = dict(os.environ, PORT=str(puerto), CARDS4PLAY_ADMIN_PASSWORD=clave_admin)
    with (carpeta / "servidor.log").open("w+") as registro:
        proceso = subprocess.Popen(["java", "-cp", classpath, "api.Main"], cwd=carpeta,
                                   env=entorno, stdout=registro, stderr=subprocess.STDOUT)
        try:
            for _ in range(150):
                if proceso.poll() is not None:
                    registro.seek(0)
                    raise RuntimeError("La API terminó al arrancar.\n" + registro.read())
                try:
                    with urlopen(url + "/health", timeout=1) as salud:
                        if salud.read().decode() == "API funcionando":
                            break
                except (URLError, TimeoutError):
                    pass
                time.sleep(0.1)
            else:
                raise RuntimeError("La API no respondió a /health")
            yield url
        finally:
            proceso.terminate()
            try:
                proceso.wait(timeout=10)
            except subprocess.TimeoutExpired:
                proceso.kill()
                proceso.wait()


def probar_archivo_anterior(carpeta, classpath):
    archivo = carpeta / "data" / "usuarios.json"
    archivo.parent.mkdir(parents=True)
    acceso = {"username": "cliente-anterior", "password": "clave-del-formato-anterior"}
    antiguo = dict(acceso, id="cliente-01", rol="CLIENTE", comprasIds=["compra-01"])
    archivo.write_text(json.dumps([antiguo]), encoding="utf-8")

    with servidor(carpeta, classpath, "clave-de-prueba-admin") as url:
        token = pedir(url, "/auth/login", 200, acceso)["token"]
        perfil = pedir(url, "/clientes/cliente-01", 200, token=token)
        assert perfil["rol"] == "CLIENTE"
    contenido = archivo.read_text(encoding="utf-8")
    guardado = next(cuenta for cuenta in json.loads(contenido) if cuenta["id"] == "cliente-01")
    assert guardado["comprasIds"] == ["compra-01"] and "clave" in guardado
    assert acceso["password"] not in contenido and "password" not in guardado
    print("4. Una cuenta del formato anterior conservó su id, su clave de acceso y sus referencias.")


def probar():
    admin = {"username": "admin", "password": "clave-de-la-prueba-admin"}
    cliente = {"username": "cliente-demo", "password": "clave-de-la-prueba-cliente"}
    with tempfile.TemporaryDirectory() as temporal:
        carpeta = Path(temporal)
        dependencias = carpeta / "dependencias.txt"
        subprocess.run(["mvn", "-q", "compile", "org.apache.maven.plugins:maven-dependency-plugin:3.8.1:build-classpath",
                        f"-Dmdep.outputFile={dependencias}"], cwd=PROYECTO, check=True)
        classpath = str(PROYECTO / "target" / "classes") + os.pathsep + dependencias.read_text().strip()

        with servidor(carpeta, classpath, admin["password"]) as url:
            token_admin = pedir(url, "/auth/login", 200, admin)["token"]
            registrado = pedir(url, "/clientes", 201, cliente, token_admin)
            ruta_perfil = "/clientes/" + registrado["id"]
            token_anterior = pedir(url, "/auth/login", 200, cliente)["token"]
            print("1. El administrador registró al cliente y el cliente inició sesión.")

        archivo = carpeta / "data" / "usuarios.json"
        cuentas = json.loads(archivo.read_text(encoding="utf-8"))
        assert len(cuentas) == 2
        assert all("password" not in cuenta and "clave" in cuenta for cuenta in cuentas)
        assert all(c["password"] not in archivo.read_text(encoding="utf-8") for c in (admin, cliente))

        with servidor(carpeta, classpath, "otra-clave-de-inicio") as url:
            pedir(url, ruta_perfil, 401, token=token_anterior)
            token_admin = pedir(url, "/auth/login", 200, admin)["token"]
            token_nuevo = pedir(url, "/auth/login", 200, cliente)["token"]
            assert pedir(url, ruta_perfil, 200, token=token_nuevo) == registrado
            print("2. Tras el reinicio, el mismo cliente recuperó su perfil con su contraseña.")
            pedir(url, "/clientes", 409, dict(cliente, username="CLIENTE-DEMO"), token_admin)
            pedir(url, "/clientes", 403, cliente, token_nuevo)
            pedir(url, "/auth/login", 401, dict(cliente, password="equivocada"))
            assert len(json.loads(archivo.read_text(encoding="utf-8"))) == 2
            print("3. Se conservaron las dos cuentas, sus roles y la protección contra duplicados.")
        probar_archivo_anterior(carpeta / "version-anterior", classpath)


if __name__ == "__main__":
    probar()
