package api.controller;

import static spark.Spark.get;

public class HealthController {

    public void registrarRutas() {
        get("/health", (request, response) -> {
            response.status(200);
            response.type("text/plain; charset=utf-8");
            return "API funcionando";
        });
    }
}
