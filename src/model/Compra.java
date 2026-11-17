package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Representa la factura de una compra; sus renglones están en DetalleCompra. */
public class Compra {
    private final String id;
    private final String clienteId;
    private final LocalDateTime fecha;
    private final List<DetalleCompra> detalles = new ArrayList<>();
    private FuenteTasa fuenteTasa;

    public Compra(String id, String clienteId, LocalDateTime fecha, FuenteTasa fuenteTasa) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El id es obligatorio");
        if (clienteId == null || clienteId.isBlank()) throw new IllegalArgumentException("El cliente es obligatorio");
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
        this.fuenteTasa = fuenteTasa;
    }

    public void agregarDetalle(DetalleCompra detalle) {
        if (detalle == null) throw new IllegalArgumentException("El detalle es obligatorio");
        detalles.add(detalle);
    }

    public String getId() { return id; }
    public String getClienteId() { return clienteId; }
    public LocalDateTime getFecha() { return fecha; }
    public List<DetalleCompra> getDetalles() { return Collections.unmodifiableList(detalles); }
    public FuenteTasa getFuenteTasa() { return fuenteTasa; }
    public void setFuenteTasa(FuenteTasa fuenteTasa) { this.fuenteTasa = fuenteTasa; }

    public double getTotalCop() {
        return detalles.stream().mapToDouble(DetalleCompra::getSubtotalCop).sum();
    }
}
