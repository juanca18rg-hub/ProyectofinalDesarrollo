package model;

public enum Rareza {
    N("N", 50),
    UC("UC", 25),
    R("R", 15),
    SR("SR", 7),
    UR("UR", 3);

    private final String codigo;
    private final int probabilidad;

    Rareza(String codigo, int probabilidad) {
        this.codigo = codigo;
        this.probabilidad = probabilidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getProbabilidad() {
        return probabilidad;
    }

    public static int sumaProbabilidades() {
        int total = 0;
        for (Rareza rareza : values()) {
            total += rareza.probabilidad;
        }
        return total;
    }
}
