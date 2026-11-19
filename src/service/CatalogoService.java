package service;

import model.Inventario;
import model.Producto;

import java.util.List;

public class CatalogoService {
    private final Inventario inventario = new Inventario();

    public synchronized void agregar(Producto producto) {
        inventario.agregar(producto);
    }

    public synchronized Producto buscar(String id) {
        return inventario.buscar(id).orElse(null);
    }

    public synchronized List<Producto> listar() {
        return List.copyOf(inventario.listar());
    }
}
