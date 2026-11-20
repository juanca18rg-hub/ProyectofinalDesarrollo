# Cards4Play API

Base Maven para desarrollar el backend del proyecto final en Java 17, siguiendo la estructura sencilla del taller de Tienda (`src/model`, `src/service`, etc.).

## Estructura inicial

- `src/model`: entidades, enumeraciones y reglas del dominio.
- `src/service`: lógica de aplicación que iremos implementando.
- `src/repository`: persistencia JSON en archivos.
- `src/api`: rutas REST con Spark.
- `src/util`: utilidades compartidas.
- `data`: archivos JSON persistentes; no se deben subir datos sensibles.

## Dependencias

Maven descarga Spark Java 2.9.4 y Gson 2.10.1. Java 17 es la versión de compilación configurada. En VS Code, abre esta carpeta como proyecto Java/Maven y carga el proyecto Maven cuando aparezca la notificación.

## Nota de diseño

El archivo del proyecto final pide persistencia JSON. El taller de Tienda sirve como guía para Maven, Spark, Gson y Docker, pero su persistencia de texto separado por comas no se debe copiar para Cards4Play.

## Persistencia de usuarios

Abre el proyecto en VS Code y usa **Run Java** en `src/api/Main.java`. La API escucha en el puerto 4567 por defecto. Al registrar un cliente, `UsuarioRepository` guarda su cuenta en `data/usuarios.json`. Al arrancar de nuevo desde la misma carpeta, `AuthService` puede autenticar a ese cliente con su contraseña. Los tokens duran hasta que se cierre la aplicación.

La primera vez se crea el usuario `admin`. Si defines `CARDS4PLAY_ADMIN_PASSWORD` antes de iniciar la API, se usa esa contraseña. Si dejas la variable vacía, la consola muestra una clave inicial generada una sola vez. Cuando la cuenta ya existe, conserva su contraseña. En un servidor compartido, configura la variable antes del primer inicio.

El archivo guarda una sal aleatoria y una huella de la contraseña calculada con PBKDF2. Si ya tienes cuentas del formato anterior con el campo `password`, la aplicación las convierte al iniciar y conserva sus identificadores y roles.

Puedes probar este bloque en la terminal desde la carpeta del proyecto. La prueba utiliza Maven, Java y Python 3. Arranca y detiene la API con datos temporales y prueba el login antes y después del reinicio.

```bash
python3 pruebas/persistencia_usuarios.py
```
