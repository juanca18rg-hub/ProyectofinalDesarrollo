
package repository;

import com.google.gson.*;
import model.Compra;
import model.DetalleCompra;
import model.FuenteTasa;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompraRepository {

    private final Path archivo;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public CompraRepository() {
        this(Path.of("data", "compras.json"));
    }

    public CompraRepository(Path archivo) {
        this.archivo = archivo.toAbsolutePath();
    }

    public List<Compra> cargar() {

        List<Compra> compras = new ArrayList<>();

        if (Files.notExists(archivo)) {
            return compras;
        }

        try {
            String contenido = Files.readString(archivo);

            JsonArray registros = JsonParser
                    .parseString(contenido)
                    .getAsJsonArray();

            for (JsonElement registro : registros) {

                JsonObject datos = registro.getAsJsonObject();

                Compra compra = new Compra(
                        datos.get("id").getAsString(),
                        datos.get("clienteId").getAsString(),
                        LocalDateTime.parse(
                                datos.get("fecha").getAsString()),
                        FuenteTasa.valueOf(
                                datos.get("fuenteTasa").getAsString())
                );

                for (JsonElement elemento :
                        datos.getAsJsonArray("detalles")) {

                    JsonObject detalle =
                            elemento.getAsJsonObject();

                    compra.agregarDetalle(new DetalleCompra(
                            detalle.get("productoId").getAsString(),
                            detalle.get("cantidad").getAsInt(),
                            detalle.get("precioUnitarioUsd").getAsDouble(),
                            detalle.get("precioUnitarioCop").getAsDouble()
                    ));
                }

                compras.add(compra);
            }

            return compras;

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudieron cargar las compras", e);

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "El archivo de compras contiene datos invalidos", e);
        }
    }

    public void guardar(List<Compra> compras) {

        JsonArray registros = new JsonArray();

        for (Compra compra : compras) {

            JsonObject datos = new JsonObject();

            datos.addProperty("id", compra.getId());
            datos.addProperty("clienteId", compra.getClienteId());
            datos.addProperty("fecha", compra.getFecha().toString());

            datos.addProperty(
                    "fuenteTasa",
                    compra.getFuenteTasa().name()
            );

            datos.addProperty("totalCop", compra.getTotalCop());

            JsonArray detalles = new JsonArray();

            for (DetalleCompra detalle : compra.getDetalles()) {

                JsonObject item = new JsonObject();

                item.addProperty(
                        "productoId", detalle.getProductoId());

                item.addProperty(
                        "cantidad", detalle.getCantidad());

                item.addProperty(
                        "precioUnitarioUsd",
                        detalle.getPrecioUnitarioUsd());

                item.addProperty(
                        "precioUnitarioCop",
                        detalle.getPrecioUnitarioCop());

                detalles.add(item);
            }

            datos.add("detalles", detalles);
            registros.add(datos);
        }

        try {
            Files.createDirectories(archivo.getParent());

            Path temporal = Files.createTempFile(
                    archivo.getParent(), "compras-", ".tmp");

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
                    "No se pudieron guardar las compras", e);
        }
    }
}
