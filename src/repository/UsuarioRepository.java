package repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Administrador;
import model.ClaveUsuario;
import model.Cliente;
import model.Rol;
import model.Usuario;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    private final Path archivo;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Usuario> usuarios = new ArrayList<>();

    public UsuarioRepository() {
        this(Path.of("data", "usuarios.json"));
    }

    public UsuarioRepository(Path archivo) {
        this.archivo = archivo.toAbsolutePath();
        cargar();
    }

    public synchronized Optional<Usuario> buscarPorUsername(String username) {
        return usuarios.stream()
                .filter(usuario -> usuario.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public synchronized Optional<Usuario> buscarPorId(String id) {
        return usuarios.stream().filter(usuario -> usuario.getId().equals(id)).findFirst();
    }

    public synchronized void guardar(Usuario nuevo) {
        if (existe(nuevo)) {
            throw new IllegalStateException("El usuario ya existe");
        }
        List<Usuario> actualizados = new ArrayList<>(usuarios);
        actualizados.add(nuevo);
        escribir(actualizados);
        usuarios.add(nuevo);
    }

    private boolean existe(Usuario usuario) {
        return buscarPorId(usuario.getId()).isPresent()
                || buscarPorUsername(usuario.getUsername()).isPresent();
    }

    private void cargar() {
        if (Files.notExists(archivo)) {
            return;
        }

        boolean hayClavesAntiguas = false;
        try (Reader entrada = Files.newBufferedReader(archivo)) {
            for (JsonElement registro : JsonParser.parseReader(entrada).getAsJsonArray()) {
                JsonObject datos = registro.getAsJsonObject();
                hayClavesAntiguas = hayClavesAntiguas || datos.has("password");
                Usuario usuario = leerUsuario(datos);
                if (existe(usuario)) {
                    throw new IllegalArgumentException("Hay cuentas duplicadas en el archivo de usuarios");
                }
                usuarios.add(usuario);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + archivo, e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("El archivo de usuarios contiene datos inválidos: " + archivo, e);
        }
        if (hayClavesAntiguas) {
            escribir(usuarios);
        }
    }

    private Usuario leerUsuario(JsonObject datos) {
        if (!datos.has("rol") && datos.has("tipo")) {
            datos.add("rol", datos.get("tipo"));
        }
        if (datos.has("password") && !datos.has("clave")) {
            ClaveUsuario clave = new ClaveUsuario(datos.get("password").getAsString());
            datos.add("clave", gson.toJsonTree(clave));
        }
        Rol rol = Rol.valueOf(datos.get("rol").getAsString());
        List<String> listas = rol == Rol.ADMIN
                ? List.of("productosRegistradosIds", "torneosCreadosIds") : List.of("comprasIds");
        for (String lista : listas) {
            if (!datos.has(lista) || datos.get(lista).isJsonNull()) {
                datos.add(lista, new JsonArray());
            }
        }
        Usuario usuario = switch (rol) {
            case ADMIN -> gson.fromJson(datos, Administrador.class);
            case CLIENTE -> gson.fromJson(datos, Cliente.class);
        };
        usuario.validar();
        return usuario;
    }

    private void escribir(List<Usuario> lista) {
        try {
            Files.createDirectories(archivo.getParent());
            Path temporal = Files.createTempFile(archivo.getParent(), "usuarios-", ".tmp");
            try {
                Files.writeString(temporal, gson.toJson(lista));
                Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporal);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo de usuarios", e);
        }
    }
}
