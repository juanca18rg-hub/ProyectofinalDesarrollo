package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cliente extends Usuario {
    private final List<String> comprasIds = new ArrayList<>();

    public Cliente(String id, String username, String password) {
        super(id, username, password, Rol.CLIENTE);
    }

    public void registrarCompra(Compra compra) {
        if (!compra.getClienteId().equals(getId())) {
            throw new IllegalArgumentException("La compra pertenece a otro cliente");
        }
        comprasIds.add(compra.getId());
    }

    public List<String> getComprasIds() {
        return Collections.unmodifiableList(comprasIds);
    }
}
