package service;

import model.Booster;
import model.Compra;
import model.DetalleCompra;
import model.FuenteTasa;
import model.Producto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompraService {
    private final CatalogoService catalogo;
    private final List<Compra> compras = new ArrayList<>();

    public CompraService(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    public Compra registrar(String clienteId, Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        synchronized (catalogo) {
            if (producto instanceof Booster) {
                if (cantidad != 1) {
                    throw new IllegalArgumentException("Cada booster es una única unidad");
                }
                if (catalogo.duenioBooster(producto.getId()) != null) {
                    throw new IllegalStateException("El booster ya fue vendido");
                }
            }

            String configuracion = System.getenv("TASA_DEFECTO");
            double tasa = configuracion == null ? 4000 : Double.parseDouble(configuracion);
            Compra compra = new Compra(UUID.randomUUID().toString(), clienteId,
                    LocalDateTime.now(), FuenteTasa.DEFECTO);
            compra.agregarDetalle(new DetalleCompra(producto.getId(), cantidad,
                    producto.getPrecioUsd(), producto.getPrecioCop(tasa)));
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
