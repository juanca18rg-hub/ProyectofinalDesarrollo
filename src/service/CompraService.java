
package service;

import model.Booster;
import model.Compra;
import model.DetalleCompra;
import model.Producto;
import model.TasaCambio;
import repository.CompraRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CompraService {

    private final CatalogoService catalogo;
    private final TasaService tasas;
    private final CompraRepository repository;

    private final List<Compra> compras = new ArrayList<>();

    public CompraService(CatalogoService catalogo,
                         TasaService tasas) {
        this(catalogo, tasas, null);
    }

    public CompraService(CatalogoService catalogo,
                         TasaService tasas,
                         CompraRepository repository) {

        this.catalogo = catalogo;
        this.tasas = tasas;
        this.repository = repository;

        if (repository != null) {
            compras.addAll(repository.cargar());
            recuperarPropietarios();
        }
    }

    private void recuperarPropietarios() {

        Set<String> ids = new HashSet<>();

        for (Compra compra : compras) {

            if (!ids.add(compra.getId())) {
                throw new IllegalStateException(
                        "Existe una compra duplicada");
            }

            for (DetalleCompra detalle : compra.getDetalles()) {

                Producto producto =
                        catalogo.buscar(detalle.getProductoId());

                if (producto == null) {
                    throw new IllegalStateException(
                            "La compra contiene un producto inexistente");
                }

                if (producto instanceof Booster) {

                    if (detalle.getCantidad() != 1
                            || catalogo.duenioBooster(producto.getId()) != null) {

                        throw new IllegalStateException(
                                "Venta de booster invalida o duplicada");
                    }

                    catalogo.asignarBooster(
                            producto.getId(),
                            compra.getClienteId()
                    );
                }
            }
        }
    }

    public Compra registrar(String clienteId,
                            Producto producto,
                            int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero");
        }

        TasaCambio tasa = tasas.obtenerTasa();

        synchronized (catalogo) {

            if (producto instanceof Booster) {

                if (cantidad != 1) {
                    throw new IllegalArgumentException(
                            "Cada booster es una unica unidad");
                }

                if (catalogo.duenioBooster(producto.getId()) != null) {
                    throw new IllegalStateException(
                            "El booster ya fue vendido");
                }
            }

            double precioCop = Math.round(
                    producto.getPrecioCop(tasa.getValor()) * 100.0
            ) / 100.0;

            Compra compra = new Compra(
                    UUID.randomUUID().toString(),
                    clienteId,
                    LocalDateTime.now(),
                    tasa.getFuente()
            );

            compra.agregarDetalle(new DetalleCompra(
                    producto.getId(),
                    cantidad,
                    producto.getPrecioUsd(),
                    precioCop
            ));

            List<Compra> actualizadas =
                    new ArrayList<>(compras);

            actualizadas.add(compra);

            if (repository != null) {
                repository.guardar(actualizadas);
            }

            compras.add(compra);

            if (producto instanceof Booster) {
                catalogo.asignarBooster(
                        producto.getId(), clienteId);
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
