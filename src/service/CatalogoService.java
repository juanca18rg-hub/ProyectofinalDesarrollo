package service;

import model.Inventario;
import model.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogoService {
    private final Inventario inventario = new Inventario();

    private final Map<String, String> dueniosBooster = new HashMap<>();

    public synchronized String duenioBooster(String id) {
        return dueniosBooster.get(id);
    }

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
