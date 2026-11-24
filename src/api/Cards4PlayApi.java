package api;

import api.controller.AuthController;
import api.controller.UsuarioController;
import api.controller.ProductoController;
import api.controller.BoosterController;
import api.controller.InventarioController;
import api.controller.CompraController;
import api.controller.TorneoController;
import api.controller.HealthController;
import service.AuthService;
import static spark.Spark.port;

public class Cards4PlayApi {

    public static void iniciar() {
        AuthService auth = new AuthService(System.getenv("CARDS4PLAY_ADMIN_PASSWORD"));
        String puerto = System.getenv("PORT");
        port(puerto == null || puerto.isBlank() ? 4567 : Integer.parseInt(puerto));

        HealthController healthController = new HealthController();
        healthController.registrarRutas();
        new AuthController(auth).registrarRutas();
        new UsuarioController(auth).registrarRutas();
        service.CatalogoService catalogo = new service.CatalogoService();
        new ProductoController(auth, catalogo).registrarRutas();
        new BoosterController(auth, catalogo).registrarRutas();
        new InventarioController(auth, catalogo).registrarRutas();
        new CompraController(auth, catalogo).registrarRutas();
        new TorneoController(auth).registrarRutas();
    }
}
