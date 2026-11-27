
package repository;

import com.google.gson.*;
import model.Torneo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TorneoRepository {

    private final Path archivo;

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public TorneoRepository() {
        this(Path.of("data", "torneos.json"));
    }

    public TorneoRepository(Path archivo) {
        this.archivo = archivo.toAbsolutePath();
    }

    public List<Torneo> cargar() {

        List<Torneo> torneos = new ArrayList<>();

        if (Files.notExists(archivo)) {
            return torneos;
        }

        try {
            String contenido = Files.readString(archivo);

            JsonArray registros = JsonParser
                    .parseString(contenido)
                    .getAsJsonArray();

            for (JsonElement registro : registros) {

                JsonObject datos = registro.getAsJsonObject();

                List<String> participantes = new ArrayList<>();

                for (JsonElement participante :
                        datos.getAsJsonArray("participantesIds")) {

                    participantes.add(participante.getAsString());
                }

                Torneo torneo = new Torneo(
                        datos.get("id").getAsString(),
                        datos.get("nombre").getAsString(),

                        LocalDateTime.parse(
                                datos.get("inicioInscripciones")
                                        .getAsString()),

                        LocalDateTime.parse(
                                datos.get("finInscripciones")
                                        .getAsString()),

                        LocalDate.parse(
                                datos.get("fechaEvento")
                                        .getAsString()),

                        datos.get("maxParticipantes").getAsInt(),

                        participantes
                );

                torneos.add(torneo);
            }

            return torneos;

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "No se pudieron cargar los torneos", e);

        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "El archivo de torneos contiene datos invalidos", e);
        }
    }

    public void guardar(List<Torneo> torneos) {

        JsonArray registros = new JsonArray();

        for (Torneo torneo : torneos) {

            JsonObject datos = new JsonObject();

            datos.addProperty("id", torneo.getId());
            datos.addProperty("nombre", torneo.getNombre());

            datos.addProperty(
                    "inicioInscripciones",
                    torneo.getInicioInscripciones().toString());

            datos.addProperty(
                    "finInscripciones",
                    torneo.getFinInscripciones().toString());

            datos.addProperty(
                    "fechaEvento",
                    torneo.getFechaEvento().toString());

            datos.addProperty(
                    "maxParticipantes",
                    torneo.getMaxParticipantes());

            JsonArray participantes = new JsonArray();

            for (String participante :
                    torneo.getParticipantesIds()) {

                participantes.add(participante);
            }

            datos.add("participantesIds", participantes);

            registros.add(datos);
        }

        try {
            Files.createDirectories(archivo.getParent());

            Path temporal = Files.createTempFile(
                    archivo.getParent(), "torneos-", ".tmp");

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
                    "No se pudieron guardar los torneos", e);
        }
    }
}
