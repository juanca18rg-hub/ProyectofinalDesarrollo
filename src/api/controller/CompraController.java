package api.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Booster;
import model.Compra;
import model.DetalleCompra;
import model.Producto;
import model.Rol;
import model.Usuario;
import service.AuthService;
import service.CatalogoService;
import service.CompraService;
import service.TasaService;
import static spark.Spark.get;
import static spark.Spark.post;

public class CompraController {
    private final AuthService auth;
    private final CatalogoService catalogo;
    private final CompraService compras;

    public CompraController(AuthService auth, CatalogoService catalogo, TasaService tasas) {
        this.auth = auth;
        this.catalogo = catalogo;
        this.compras = new CompraService(catalogo, tasas);
    }

    public void registrarRutas() {
        post("/compras", (req, res) -> {
            Usuario cliente = Respuestas.usuario(req, res, auth, Rol.CLIENTE);
            DatosCompra datos = Respuestas.leer(req, DatosCompra.class);
            Respuestas.exigir(datos.productoId != null && !datos.productoId.isBlank(),
                    "El id del producto es obligatorio");
            Producto producto = catalogo.buscar(datos.productoId);
            if (producto == null) {
                Respuestas.error(res, 404, "Producto inexistente");
            }
            Compra compra = compras.registrar(cliente.getId(), producto, datos.cantidad);
            return Respuestas.json(res, 201, verCompra(compra));
        });

        get("/clientes/:id/compras", (req, res) -> {
            Usuario cliente = Respuestas.usuario(req, res, auth, Rol.CLIENTE);
            if (!cliente.getId().equals(req.params(":id"))) {
                Respuestas.error(res, 403, "Solo puedes consultar tu historial");
            }

            List<Map<String, Object>> historial = new ArrayList<>();
            for (Compra compra : compras.historial(cliente.getId())) {
                historial.add(verCompra(compra));
            }
            return Respuestas.json(res, 200, historial);
        });
    }

    private Map<String, Object> verCompra(Compra compra) {
        List<Map<String, Object>> detalles = new ArrayList<>();
        synchronized (catalogo) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                Map<String, Object> datos = new LinkedHashMap<>();
                datos.put("productoId", detalle.getProductoId());
                datos.put("cantidad", detalle.getCantidad());
                datos.put("precioUnitarioUsd", detalle.getPrecioUnitarioUsd());
                datos.put("precioUnitarioCop", detalle.getPrecioUnitarioCop());
                Producto producto = catalogo.buscar(detalle.getProductoId());
                if (producto instanceof Booster booster && booster.estaAbierto()) {
                    datos.put("cartas", booster.getCartas());
                }
                detalles.add(datos);
            }
        }
        return Map.of("id", compra.getId(), "clienteId", compra.getClienteId(),
                "fecha", compra.getFecha().toString(), "fuenteTasa", compra.getFuenteTasa(),
                "totalCop", compra.getTotalCop(), "detalles", detalles);
    }

    private static class DatosCompra {
        String productoId;
        int cantidad;
    }
}
// -------------------
