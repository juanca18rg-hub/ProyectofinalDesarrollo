package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Administrador extends Usuario {
    private final List<String> productosRegistradosIds = new ArrayList<>();
    private final List<String> torneosCreadosIds = new ArrayList<>();

    public Administrador(String id, String username, String password) {
        super(id, username, password, Rol.ADMIN);
    }

    public void registrarProducto(Producto producto, Inventario inventario) {
        inventario.agregar(producto);
        productosRegistradosIds.add(producto.getId());
    }

    public void registrarTorneo(Torneo torneo, List<Torneo> torneos) {
        if (torneos.stream().anyMatch(t -> t.getId().equals(torneo.getId()))) {
            throw new IllegalArgumentException("Ya existe un torneo con ese id");
        }
        torneos.add(torneo);
        torneosCreadosIds.add(torneo.getId());
    }

    public List<String> getProductosRegistradosIds() {
        return Collections.unmodifiableList(productosRegistradosIds);
    }

    public List<String> getTorneosCreadosIds() {
        return Collections.unmodifiableList(torneosCreadosIds);
    }
}
