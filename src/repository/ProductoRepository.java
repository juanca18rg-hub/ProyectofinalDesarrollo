
package repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import model.Accesorio;
import model.Booster;
import model.Carta;
import model.Producto;
import model.ProductoSellado;
import model.Rareza;

public class ProductoRepository {

    private final Path archivo;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public ProductoRepository() {
        this(Path.of("data", "productos.json"));
    }

    public ProductoRepository(Path archivo) {
        this.archivo = archivo.toAbsolutePath();
    }

    // Cargar los productos al iniciar la aplicación
    public List<Producto> cargar() {

        List<Producto> productos = new ArrayList<>();

        if (Files.notExists(archivo)) {
            return productos;
        }

        try {
            String contenido = Files.readString(archivo);

            JsonArray registros = JsonParser
                    .parseString(contenido)
                    .getAsJsonArray();

            for (JsonElement registro : registros) {
                productos.add(
                        leerProducto(registro.getAsJsonObject())
                );
            }

            return productos;

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudieron cargar los productos", e);

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "El archivo de productos contiene datos invalidos", e);
        }
    }

    // Guardar todos los productos
    public void guardar(List<Producto> productos) {

        JsonArray registros = new JsonArray();

        for (Producto producto : productos) {
            registros.add(convertirJson(producto));
        }

        try {
            Files.createDirectories(archivo.getParent());

            Path temporal = Files.createTempFile(
                    archivo.getParent(), "productos-", ".tmp");

            try {
                Files.writeString(
                        temporal, gson.toJson(registros));

                Files.move(
                        temporal,
                        archivo,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } finally {
                Files.deleteIfExists(temporal);
            }

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudieron guardar los productos", e);
        }
    }

    // Convertir un producto a JSON
    private JsonObject convertirJson(Producto producto) {

        JsonObject datos = new JsonObject();

        datos.addProperty("id", producto.getId());
        datos.addProperty("nombre", producto.getNombre());
        datos.addProperty("precioUsd", producto.getPrecioUsd());
        datos.addProperty(
                "tipo", producto.getClass().getSimpleName());

        if (producto instanceof Carta carta) {

            datos.addProperty(
                    "rareza", carta.getRareza().name());

        } else if (producto instanceof Booster booster) {

            JsonArray cartas = new JsonArray();

            for (Carta carta : booster.getCartas()) {
                cartas.add(convertirJson(carta));
            }

            datos.add("cartas", cartas);

            if (booster.getFechaApertura() != null) {
                datos.addProperty(
                        "fechaApertura",
                        booster.getFechaApertura().toString()
                );
            }
        }

        return datos;
    }

    // Reconstruir un producto desde el JSON
    private Producto leerProducto(JsonObject datos) {

        String id = datos.get("id").getAsString();
        String nombre = datos.get("nombre").getAsString();
        double precioUsd = datos.get("precioUsd").getAsDouble();
        String tipo = datos.get("tipo").getAsString();

        switch (tipo) {

            case "Carta":
                return new Carta(
                        id,
                        nombre,
                        precioUsd,
                        Rareza.valueOf(
                                datos.get("rareza").getAsString())
                );

            case "Accesorio":
                return new Accesorio(id, nombre, precioUsd);

            case "ProductoSellado":
                return new ProductoSellado(id, nombre, precioUsd);

            case "Booster":

                List<Carta> cartas = new ArrayList<>();

                for (JsonElement elemento :
                        datos.getAsJsonArray("cartas")) {

                    cartas.add((Carta) leerProducto(
                            elemento.getAsJsonObject()));
                }

                LocalDateTime fechaApertura = null;

                if (datos.has("fechaApertura")
                        && !datos.get("fechaApertura").isJsonNull()) {

                    fechaApertura = LocalDateTime.parse(
                            datos.get("fechaApertura").getAsString());
                }

                return new Booster(
                        id, nombre, precioUsd,
                        cartas, fechaApertura);

            default:
                throw new IllegalArgumentException(
                        "Tipo de producto desconocido: " + tipo);
        }
    }
}
