package api.controller;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import model.Rol;
import model.Usuario;
import service.AuthService;
import spark.Request;
import spark.Response;

import java.time.DateTimeException;
import java.util.Map;

import static spark.Spark.exception;
import static spark.Spark.halt;

final class Respuestas {
    private static final Gson GSON = new Gson();

    private Respuestas() {}

    static String json(Response respuesta, int codigo, Object datos) {
        respuesta.status(codigo);
        respuesta.type("application/json; charset=utf-8");
        return GSON.toJson(datos);
    }

    static <T> T leer(Request solicitud, Class<T> clase) {
        T datos = GSON.fromJson(solicitud.body(), clase);
        if (datos == null) {
            throw new IllegalArgumentException("El cuerpo JSON es obligatorio");
        }
        return datos;
    }

    static void exigir(boolean valido, String mensaje) {
        if (!valido) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    static void error(Response respuesta, int codigo, String mensaje) {
        halt(codigo, json(respuesta, codigo, Map.of("error", mensaje)));
    }

    static Usuario usuario(Request solicitud, Response respuesta, AuthService auth, Rol rol) {
        String cabecera = solicitud.headers("Authorization");
        Usuario usuario = null;
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            usuario = auth.buscarSesion(cabecera.substring(7));
        }
        if (usuario == null) {
            error(respuesta, 401, "Debes iniciar sesión");
        }
        if (rol != null && usuario.getRol() != rol) {
            error(respuesta, 403, "No tienes permiso para esta operación");
        }
        return usuario;
    }

    static void registrarErrores() {
        exception(JsonParseException.class, (e, req, res) ->
                res.body(json(res, 400, Map.of("error", "JSON inválido"))));
        exception(IllegalArgumentException.class, (e, req, res) ->
                res.body(json(res, 400, Map.of("error", e.getMessage()))));
        exception(DateTimeException.class, (e, req, res) ->
                res.body(json(res, 400, Map.of("error", "Formato de fecha inválido"))));
        exception(IllegalStateException.class, (e, req, res) ->
                res.body(json(res, 409, Map.of("error", e.getMessage()))));
    }
}
