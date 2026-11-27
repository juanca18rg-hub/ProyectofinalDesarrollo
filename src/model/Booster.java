package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Booster extends Producto {
    public static final int CANTIDAD_CARTAS = 10;

    private final List<Carta> cartas;
    // Nulo mientras no se haya abierto. Evita el boolean abierto.
    private LocalDateTime fechaApertura;

public Booster(String id, String nombre, double precioUsd,
               List<Carta> cartas) {
    this(id, nombre, precioUsd, cartas, null);
}

public Booster(String id, String nombre, double precioUsd,
               List<Carta> cartas, LocalDateTime fechaApertura) {
    super(id, nombre, precioUsd);

    if (cartas == null || cartas.size() != CANTIDAD_CARTAS) {
        throw new IllegalArgumentException(
                "Un booster debe contener exactamente 10 cartas");
    }

    this.cartas = new ArrayList<>(cartas);
    this.fechaApertura = fechaApertura;
}

    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public boolean estaAbierto() { return fechaApertura != null; }

    /** La compra y autorización del dueño se validan en el servicio; aquí se aplica la apertura única. */
    public List<Carta> abrir() {
        if (estaAbierto()) throw new IllegalStateException("El booster ya fue abierto");
        fechaApertura = LocalDateTime.now();
        return getCartas();
    }
}
