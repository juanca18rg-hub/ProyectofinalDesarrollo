package service;

import model.Torneo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TorneoService {
    private final List<Torneo> torneos = new ArrayList<>();

    public synchronized void crear(Torneo torneo) {
        torneos.add(torneo);
    }

    public synchronized List<Torneo> listar() {
        return List.copyOf(torneos);
    }

    public synchronized boolean inscribir(String torneoId, String clienteId) {
        for (Torneo torneo : torneos) {
            if (torneo.getId().equals(torneoId)) {
                torneo.inscribir(clienteId, LocalDateTime.now(ZoneId.of("America/Bogota")));
                return true;
            }
        }
        return false;
    }
}
