package model;

public abstract class Producto {
    private final String id;
    private final String nombre;
    private final double precioUsd;

    protected Producto(String id, String nombre, double precioUsd) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El id es obligatorio");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        if (!Double.isFinite(precioUsd) || precioUsd < 0) throw new IllegalArgumentException("El precio USD no puede ser negativo");
        this.id = id;
        this.nombre = nombre;
        this.precioUsd = precioUsd;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecioUsd() { return precioUsd; }

    public double getPrecioCop(double tasaUsdCop) {
        if (!Double.isFinite(tasaUsdCop) || tasaUsdCop <= 0) throw new IllegalArgumentException("La tasa debe ser mayor que cero");
        return precioUsd * tasaUsdCop;
    }
}
