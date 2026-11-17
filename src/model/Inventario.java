package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Inventario {
    // La colección es atributo del inventario; no es una variable constante global.
    private final List<Producto> productos = new ArrayList<>();

    public void agregar(Producto producto) {
        if (producto == null) throw new IllegalArgumentException("El producto es obligatorio");
        if (buscar(producto.getId()).isPresent()) throw new IllegalArgumentException("Ya existe un producto con ese id");
        productos.add(producto);
    }

    public Optional<Producto> buscar(String id) {
        return productos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public List<Producto> listar() {
        return Collections.unmodifiableList(productos);
    }

    public boolean eliminar(String id) {
        return productos.removeIf(p -> p.getId().equals(id));
    }
}
