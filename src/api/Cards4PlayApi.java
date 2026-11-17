package api;

import api.controller.HealthController;
import static spark.Spark.port;

public class Cards4PlayApi {

    public static void iniciar() {
        port(4567);

        HealthController healthController = new HealthController();
        healthController.registrarRutas();
    }
}