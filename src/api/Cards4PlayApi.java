package api;

import api.controller.AuthController;
import api.controller.BoosterController;
import api.controller.CompraController;
import api.controller.HealthController;
import api.controller.InventarioController;
import api.controller.ProductoController;
import api.controller.TorneoController;
import api.controller.UsuarioController;
import static spark.Spark.port;

public class Cards4PlayApi {

    public static void iniciar() {
        port(Integer.parseInt(System.getenv().getOrDefault("PORT", "4567")));

        HealthController healthController = new HealthController();
        healthController.registrarRutas();
        service.AuthService auth = new service.AuthService(System.getenv("CARDS4PLAY_ADMIN_PASSWORD"));
        new AuthController(auth).registrarRutas();
        new UsuarioController(auth).registrarRutas();
        service.CatalogoService catalogo = new service.CatalogoService(new repository.ProductoRepository());   
        service.TasaService tasas = new service.TasaService();
        new ProductoController(auth, catalogo).registrarRutas();
        new BoosterController(auth, catalogo).registrarRutas();
        new InventarioController(auth, catalogo, tasas).registrarRutas();
        new CompraController(auth, catalogo, tasas).registrarRutas();
        new TorneoController(auth).registrarRutas();
    }
}
