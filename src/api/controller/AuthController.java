package api.controller;

import api.dto.LoginRequest;
import service.AuthService;

import java.util.Map;

import static spark.Spark.post;

public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public void registrarRutas() {
        Respuestas.registrarErrores();
        post("/auth/login", (req, res) -> {
            LoginRequest datos = Respuestas.leer(req, LoginRequest.class);
            Respuestas.exigir(datos.getUsername() != null && !datos.getUsername().isBlank()
                    && datos.getPassword() != null && !datos.getPassword().isBlank(),
                    "El usuario y la contraseña son obligatorios");

            String token = auth.iniciarSesion(datos.getUsername(), datos.getPassword());
            if (token == null) {
                Respuestas.error(res, 401, "Credenciales inválidas");
            }

            return Respuestas.json(res, 200, Map.of("token", token));
        });
    }
}
