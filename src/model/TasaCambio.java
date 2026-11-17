package model;

import java.time.LocalDateTime;

/** Resultado de una consulta a la tasa USD-COP o de la tasa de respaldo. */
public class TasaCambio {
    private final double valor;
    private final FuenteTasa fuente;
    private final LocalDateTime consultadaEn;

    public TasaCambio(double valor, FuenteTasa fuente, LocalDateTime consultadaEn) {
        if (!Double.isFinite(valor) || valor <= 0) throw new IllegalArgumentException("La tasa debe ser mayor que cero");
        this.valor = valor;
        this.fuente = fuente;
        this.consultadaEn = consultadaEn;
    }

    public double getValor() { return valor; }
    public FuenteTasa getFuente() { return fuente; }
    public LocalDateTime getConsultadaEn() { return consultadaEn; }
}
