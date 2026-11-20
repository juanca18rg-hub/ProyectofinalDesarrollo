package api.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Booster;
import model.Carta;
import model.Producto;
import service.AuthService;
import service.CatalogoService;
import static spark.Spark.get;

public class InventarioController {
    private final AuthService auth;
    private final CatalogoService catalogo;

    public InventarioController(AuthService auth, CatalogoService catalogo) {
        this.auth = auth;
        this.catalogo = catalogo;
    }

    public void registrarRutas() {
        get("/productos", (req, res) -> {
            Respuestas.usuario(req, res, auth, null);
            return Respuestas.json(res, 200, listar(false));
        });
        get("/boosters/disponibles", (req, res) -> {
            Respuestas.usuario(req, res, auth, null);
            return Respuestas.json(res, 200, listar(true));
        });
    }

    private List<Map<String, Object>> listar(boolean soloBoosters) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        synchronized (catalogo) {
            for (Producto producto : catalogo.listar()) {
                boolean esBooster = producto instanceof Booster;
                if (esBooster && catalogo.duenioBooster(producto.getId()) != null) {
                    continue;
                }
                if (soloBoosters && !esBooster) {
                    continue;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", producto.getId());
                item.put("nombre", producto.getNombre());
                item.put("tipo", producto.getClass().getSimpleName());
                item.put("precioUsd", producto.getPrecioUsd());
                if (producto instanceof Carta carta) {
                    item.put("rareza", carta.getRareza());
                }
                resultado.add(item);
            }
        }
        return resultado;
    }
}//------------------------------
