
package service;

import model.Torneo;
import repository.TorneoRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TorneoService {

    private final List<Torneo> torneos = new ArrayList<>();

    private final TorneoRepository repository;

    public TorneoService() {
        this(null);
    }

    public TorneoService(TorneoRepository repository) {

        this.repository = repository;

        if (repository != null) {

            for (Torneo torneo : repository.cargar()) {

                if (buscar(torneo.getId()) != null) {
                    throw new IllegalStateException(
                            "Existe un torneo duplicado");
                }

                torneos.add(torneo);
            }
        }
    }

    public synchronized void crear(Torneo torneo) {

        if (torneo == null) {
            throw new IllegalArgumentException(
                    "El torneo es obligatorio");
        }

        if (buscar(torneo.getId()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un torneo con ese id");
        }

        List<Torneo> actualizados = new ArrayList<>(torneos);

        actualizados.add(torneo);

        if (repository != null) {
            repository.guardar(actualizados);
        }

        torneos.add(torneo);
    }

    public synchronized Torneo buscar(String id) {

        for (Torneo torneo : torneos) {

            if (torneo.getId().equals(id)) {
                return torneo;
            }
        }

        return null;
    }

    public synchronized List<Torneo> listar() {
        return List.copyOf(torneos);
    }

    public synchronized boolean inscribir(
            String torneoId, String clienteId) {

        for (int i = 0; i < torneos.size(); i++) {

            Torneo torneo = torneos.get(i);

            if (torneo.getId().equals(torneoId)) {

                Torneo actualizado = new Torneo(
                        torneo.getId(),
                        torneo.getNombre(),
                        torneo.getInicioInscripciones(),
                        torneo.getFinInscripciones(),
                        torneo.getFechaEvento(),
                        torneo.getMaxParticipantes(),
                        torneo.getParticipantesIds()
                );

                actualizado.inscribir(
                        clienteId,
                        LocalDateTime.now(
                                ZoneId.of("America/Bogota"))
                );

                List<Torneo> actualizados =
                        new ArrayList<>(torneos);

                actualizados.set(i, actualizado);

                if (repository != null) {
                    repository.guardar(actualizados);
                }

                torneos.set(i, actualizado);

                return true;
            }
        }

        return false;
    }
}
