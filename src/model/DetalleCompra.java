package model;

public class DetalleCompra {
    private final String productoId;
    private final int cantidad;
    private final double precioUnitarioUsd;
    private final double precioUnitarioCop;

    public DetalleCompra(String productoId, int cantidad, double precioUnitarioUsd, double precioUnitarioCop) {
        if (productoId == null || productoId.isBlank()) throw new IllegalArgumentException("El producto es obligatorio");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        if (precioUnitarioUsd < 0 || precioUnitarioCop < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitarioUsd = precioUnitarioUsd;
        this.precioUnitarioCop = precioUnitarioCop;
    }

    public String getProductoId() { return productoId; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitarioUsd() { return precioUnitarioUsd; }
    public double getPrecioUnitarioCop() { return precioUnitarioCop; }
    public double getSubtotalCop() { return cantidad * precioUnitarioCop; }
}
