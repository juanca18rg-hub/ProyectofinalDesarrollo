
package service;

import model.Booster;
import model.Carta;
import model.Inventario;
import model.Producto;
import repository.ProductoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogoService {

    private Inventario inventario = new Inventario();

    private final Map<String, String> dueniosBooster =
            new HashMap<>();

    private final ProductoRepository repository;

    public CatalogoService() {
        this(null);
    }

    public CatalogoService(ProductoRepository repository) {

        this.repository = repository;

        if (repository != null) {
            for (Producto producto : repository.cargar()) {
                inventario.agregar(producto);
            }
        }
    }

    public synchronized String duenioBooster(String id) {
        return dueniosBooster.get(id);
    }

    synchronized void asignarBooster(String id, String clienteId) {
        dueniosBooster.put(id, clienteId);
    }

    public synchronized void agregar(Producto producto) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio");
        }

        if (buscar(producto.getId()) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con ese id");
        }

        List<Producto> actualizados =
                new ArrayList<>(inventario.listar());

        actualizados.add(producto);

        if (repository != null) {
            repository.guardar(actualizados);
        }

        inventario.agregar(producto);
    }

    public synchronized List<Carta> abrirBooster(
            String id, String clienteId) {

        Producto producto = buscar(id);

        if (!(producto instanceof Booster booster)) {
            throw new IllegalArgumentException(
                    "Booster inexistente");
        }

        if (clienteId == null
                || !clienteId.equals(duenioBooster(id))) {

            throw new IllegalStateException(
                    "Solo el comprador puede abrir el booster");
        }

        Booster actualizado = new Booster(
                booster.getId(),
                booster.getNombre(),
                booster.getPrecioUsd(),
                booster.getCartas(),
                booster.getFechaApertura()
        );

        List<Carta> cartas = actualizado.abrir();

        Inventario nuevoInventario = new Inventario();

        for (Producto existente : inventario.listar()) {

            if (existente.getId().equals(id)) {
                nuevoInventario.agregar(actualizado);
            } else {
                nuevoInventario.agregar(existente);
            }
        }

        if (repository != null) {
            repository.guardar(nuevoInventario.listar());
        }

        inventario = nuevoInventario;

        return cartas;
    }

    public synchronized Producto buscar(String id) {
        return inventario.buscar(id).orElse(null);
    }

    public synchronized List<Producto> listar() {
        return List.copyOf(inventario.listar());
    }
}
