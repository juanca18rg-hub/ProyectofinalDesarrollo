package api.controller;

import static spark.Spark.get;

public class HealthController {

    public void registrarRutas() {
        get("/health", (request, response) -> {
            response.type("text/plain");
            return "API funcionando";
        });
    }
}