package model;

public class Carta extends Producto {
    private final Rareza rareza;

    public Carta(String id, String nombre, double precioUsd, Rareza rareza) {
        super(id, nombre, precioUsd);
        if (rareza == null) throw new IllegalArgumentException("La rareza es obligatoria");
        this.rareza = rareza;
    }

    public Rareza getRareza() { return rareza; }
}
