package api.controller;

import model.Accesorio;
import model.Carta;
import model.Producto;
import model.ProductoSellado;
import model.Rareza;
import model.Rol;
import service.AuthService;
import service.CatalogoService;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.UUID;

import static spark.Spark.post;

public class ProductoController {
    private final AuthService auth;
    private final CatalogoService catalogo;

    public ProductoController(AuthService auth, CatalogoService catalogo) {
        this.auth = auth;
        this.catalogo = catalogo;
    }

    public void registrarRutas() {
        post("/productos/cartas", (req, res) -> crear(req, res, "carta"));
        post("/productos/sellados", (req, res) -> crear(req, res, "sellado"));
        post("/productos/accesorios", (req, res) -> crear(req, res, "accesorio"));
    }

    private String crear(Request req, Response res, String tipo) {
        Respuestas.usuario(req, res, auth, Rol.ADMIN);
        DatosProducto datos = Respuestas.leer(req, DatosProducto.class);
        Respuestas.exigir(datos.precioUsd != null, "El precio USD es obligatorio");
        String id = UUID.randomUUID().toString();
        Producto producto;

        if (tipo.equals("carta")) {
            producto = new Carta(id, datos.nombre, datos.precioUsd, datos.rareza);
        } else if (tipo.equals("sellado")) {
            producto = new ProductoSellado(id, datos.nombre, datos.precioUsd);
        } else {
            producto = new Accesorio(id, datos.nombre, datos.precioUsd);
        }

        catalogo.agregar(producto);
        return Respuestas.json(res, 201, Map.of("id", id, "nombre", producto.getNombre()));
    }

    private static class DatosProducto {
        String nombre;
        Double precioUsd;
        Rareza rareza;
    }
}
