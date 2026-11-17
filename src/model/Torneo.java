package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Torneo {
    private final String id;
    private final String nombre;
    private final LocalDateTime inicioInscripciones;
    private final LocalDateTime finInscripciones;
    private final LocalDate fechaEvento;
    private final int maxParticipantes;
    private final List<String> participantesIds = new ArrayList<>();

    public Torneo(String id, String nombre, LocalDateTime inicioInscripciones,
                  LocalDateTime finInscripciones, LocalDate fechaEvento, int maxParticipantes) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El id es obligatorio");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (inicioInscripciones == null || finInscripciones == null || fechaEvento == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias");
        }
        if (finInscripciones.isBefore(inicioInscripciones)) throw new IllegalArgumentException("El cierre no puede ser anterior al inicio de inscripciones");
        if (maxParticipantes <= 0) throw new IllegalArgumentException("El cupo debe ser mayor que cero");
        this.id = id;
        this.nombre = nombre;
        this.inicioInscripciones = inicioInscripciones;
        this.finInscripciones = finInscripciones;
        this.fechaEvento = fechaEvento;
        this.maxParticipantes = maxParticipantes;
    }

    public void inscribir(String clienteId, LocalDateTime fechaActual) {
        if (clienteId == null || clienteId.isBlank()) throw new IllegalArgumentException("El cliente es obligatorio");
        if (fechaActual.isBefore(inicioInscripciones) || fechaActual.isAfter(finInscripciones)) {
            throw new IllegalStateException("Las inscripciones no están habilitadas en esta fecha");
        }
        if (participantesIds.contains(clienteId)) throw new IllegalStateException("El cliente ya está inscrito");
        if (participantesIds.size() >= maxParticipantes) throw new IllegalStateException("El torneo no tiene cupos disponibles");
        participantesIds.add(clienteId);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDateTime getInicioInscripciones() { return inicioInscripciones; }
    public LocalDateTime getFinInscripciones() { return finInscripciones; }
    public LocalDate getFechaEvento() { return fechaEvento; }
    public int getMaxParticipantes() { return maxParticipantes; }
    public List<String> getParticipantesIds() { return Collections.unmodifiableList(participantesIds); }
    public boolean hayCupo() { return participantesIds.size() < maxParticipantes; }
}
