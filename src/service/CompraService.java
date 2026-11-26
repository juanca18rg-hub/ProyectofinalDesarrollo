package service;

import model.Booster;
import model.Compra;
import model.DetalleCompra;
import model.Producto;
import model.TasaCambio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompraService {
    private final CatalogoService catalogo;
    private final TasaService tasas;
    private final List<Compra> compras = new ArrayList<>();

    public CompraService(CatalogoService catalogo, TasaService tasas) {
        this.catalogo = catalogo;
        this.tasas = tasas;
    }

    public Compra registrar(String clienteId, Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        // La tasa se consulta antes de bloquear el catálogo: la API externa puede tardar hasta su timeout.
        TasaCambio tasa = tasas.obtenerTasa();

        synchronized (catalogo) {
            if (producto instanceof Booster) {
                if (cantidad != 1) {
                    throw new IllegalArgumentException("Cada booster es una única unidad");
                }
                if (catalogo.duenioBooster(producto.getId()) != null) {
                    throw new IllegalStateException("El booster ya fue vendido");
                }
            }

            // El precio en COP queda congelado con la tasa del momento de la compra.
            double precioCop = Math.round(producto.getPrecioCop(tasa.getValor()) * 100.0) / 100.0;
            Compra compra = new Compra(UUID.randomUUID().toString(), clienteId,
                    LocalDateTime.now(), tasa.getFuente());
            compra.agregarDetalle(new DetalleCompra(producto.getId(), cantidad,
                    producto.getPrecioUsd(), precioCop));
            compras.add(compra);
            if (producto instanceof Booster) {
                catalogo.asignarBooster(producto.getId(), clienteId);
            }
            return compra;
        }
    }

    public List<Compra> historial(String clienteId) {
        List<Compra> resultado = new ArrayList<>();
        synchronized (catalogo) {
            for (Compra compra : compras) {
                if (compra.getClienteId().equals(clienteId)) {
                    resultado.add(compra);
                }
            }
        }
        return resultado;
    }
}
