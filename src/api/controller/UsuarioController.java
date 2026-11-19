package api.controller;

import api.dto.LoginRequest;
import model.Cliente;
import model.Rol;
import model.Usuario;
import service.AuthService;

import java.util.Map;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class UsuarioController {
    private final AuthService auth;

    public UsuarioController(AuthService auth) {
        this.auth = auth;
    }

    public void registrarRutas() {
        post("/clientes", (req, res) -> {
            Respuestas.usuario(req, res, auth, Rol.ADMIN);
            LoginRequest datos = Respuestas.leer(req, LoginRequest.class);
            Cliente cliente = new Cliente(UUID.randomUUID().toString(),
                    datos.getUsername(), datos.getPassword());
            auth.registrar(cliente);
            return Respuestas.json(res, 201, datosPublicos(cliente));
        });

        get("/clientes/:id", (req, res) -> {
            Usuario solicitante = Respuestas.usuario(req, res, auth, null);
            String id = req.params(":id");
            if (solicitante.getRol() != Rol.ADMIN && !solicitante.getId().equals(id)) {
                Respuestas.error(res, 403, "Solo puedes consultar tu propio perfil");
            }

            Usuario cliente = auth.buscarPorId(id);
            if (!(cliente instanceof Cliente)) {
                Respuestas.error(res, 404, "Cliente inexistente");
            }
            return Respuestas.json(res, 200, datosPublicos(cliente));
        });
    }

    private Map<String, Object> datosPublicos(Usuario usuario) {
        return Map.of("id", usuario.getId(), "username", usuario.getUsername(),
                "rol", usuario.getRol());
    }
}
