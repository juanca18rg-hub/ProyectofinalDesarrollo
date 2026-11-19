package api;

import api.controller.AuthController;
import api.controller.UsuarioController;
import api.controller.ProductoController;
import api.controller.HealthController;
import static spark.Spark.port;

public class Cards4PlayApi {

    public static void iniciar() {
        port(4567);

        HealthController healthController = new HealthController();
        healthController.registrarRutas();
        service.AuthService auth = new service.AuthService(System.getenv("CARDS4PLAY_ADMIN_PASSWORD"));
        new AuthController(auth).registrarRutas();
        new UsuarioController(auth).registrarRutas();
        service.CatalogoService catalogo = new service.CatalogoService();
        new ProductoController(auth, catalogo).registrarRutas();
    }
}