package api.controller;

import java.util.Map;

import model.Booster;
import model.Producto;
import model.Rol;
import model.Usuario;
import service.AuthService;
import service.BoosterService;
import service.CatalogoService;
import spark.Request;
import spark.Response;
import static spark.Spark.post;

public class BoosterController {
    private final AuthService auth;
    private final CatalogoService catalogo;
    private final BoosterService boosters;

    public BoosterController(AuthService auth, CatalogoService catalogo) {
        this.auth = auth;
        this.catalogo = catalogo;
        this.boosters = new BoosterService(catalogo);
    }

    public void registrarRutas() {
        post("/boosters", (req, res) -> {
            Respuestas.usuario(req, res, auth, Rol.ADMIN);
            DatosBooster datos = Respuestas.leer(req, DatosBooster.class);
            Respuestas.exigir(datos.precioUsd != null, "El precio USD es obligatorio");
            Booster booster = boosters.crear(datos.nombre, datos.precioUsd);
            return Respuestas.json(res, 201, Map.of("id", booster.getId(),
                    "nombre", booster.getNombre(), "precioUsd", booster.getPrecioUsd()));
        });

        post("/boosters/:id/abrir", this::abrir);
    }

    private String abrir(Request req, Response res) {
        Usuario cliente = Respuestas.usuario(req, res, auth, Rol.CLIENTE);
        synchronized (catalogo) {
            Producto producto = catalogo.buscar(req.params(":id"));
            if (!(producto instanceof Booster)) {
                Respuestas.error(res, 404, "Booster inexistente");
            }

            String duenioId = catalogo.duenioBooster(producto.getId());
            if (!cliente.getId().equals(duenioId)) {
                Respuestas.error(res, 403, "Solo el comprador puede abrir el booster");
            }

            return Respuestas.json(
        res,
        200,
        Map.of("cartas",
                catalogo.abrirBooster(
                        producto.getId(),
                        cliente.getId()
                ))
);
        }
    }

    private static class DatosBooster {
        String nombre;
        Double precioUsd;
    }
}
// --------------------------------------