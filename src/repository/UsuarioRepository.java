package repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.Administrador;
import model.Cliente;
import model.Rol;
import model.Usuario;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    private final Path archivo = Path.of("data", "usuarios.json");

    private final Type tipoLista = new TypeToken<List<Usuario>>() {}.getType();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Usuario.class, new UsuarioAdapter())
            .create();

    public Optional<Usuario> buscarPorUsername(String username) {
        return cargarTodos().stream()
                .filter(usuario -> usuario.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public void guardar(Usuario nuevoUsuario) {
        List<Usuario> usuarios = cargarTodos();

        boolean yaExiste = usuarios.stream().anyMatch(usuario ->
                usuario.getId().equals(nuevoUsuario.getId())
                        || usuario.getUsername().equalsIgnoreCase(nuevoUsuario.getUsername())
        );

        if (yaExiste) {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID o nombre de usuario");
        }

        usuarios.add(nuevoUsuario);
        escribirTodos(usuarios);
    }

    private List<Usuario> cargarTodos() {
        try {
            Files.createDirectories(archivo.getParent());

            if (Files.notExists(archivo)) {
                return new ArrayList<>();
            }

            try (Reader reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                List<Usuario> usuarios = gson.fromJson(reader, tipoLista);
                return usuarios == null ? new ArrayList<>() : usuarios;
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron leer los usuarios", e);
        }
    }

    private void escribirTodos(List<Usuario> usuarios) {
        try {
            Files.createDirectories(archivo.getParent());
            Files.writeString(
                    archivo,
                    gson.toJson(usuarios),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar los usuarios", e);
        }
    }

    private static class UsuarioAdapter
            implements JsonSerializer<Usuario>, JsonDeserializer<Usuario> {

        @Override
        public JsonElement serialize(
                Usuario usuario,
                Type tipo,
                JsonSerializationContext contexto
        ) {
            JsonObject json = contexto.serialize(usuario, usuario.getClass()).getAsJsonObject();
            json.addProperty("tipo", usuario.getRol().name());
            return json;
        }

        @Override
        public Usuario deserialize(
                JsonElement elemento,
                Type tipo,
                JsonDeserializationContext contexto
        ) throws JsonParseException {
            JsonObject json = elemento.getAsJsonObject();
            String tipoUsuario = json.get("tipo").getAsString();
            json.remove("tipo");

            return switch (Rol.valueOf(tipoUsuario)) {
                case ADMIN -> contexto.deserialize(json, Administrador.class);
                case CLIENTE -> contexto.deserialize(json, Cliente.class);
            };
        }
    }
}