package service;

import model.Administrador;
import model.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    private final List<Usuario> usuarios = new ArrayList<>();
    private final Map<String, Usuario> sesiones = new HashMap<>();

    public AuthService(String claveAdmin) {
        if (claveAdmin != null && !claveAdmin.isBlank()) {
            usuarios.add(new Administrador("admin", "admin", claveAdmin));
        }
    }

    public synchronized String iniciarSesion(String username, String password) {
        for (Usuario usuario : usuarios) {
            if (usuario.getUsername().equalsIgnoreCase(username) && usuario.autenticar(password)) {
                String token = UUID.randomUUID().toString();
                sesiones.put(token, usuario);
                return token;
            }
        }
        return null;
    }

    public synchronized Usuario buscarSesion(String token) {
        return sesiones.get(token);
    }

    public synchronized Usuario buscarPorId(String id) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId().equals(id)) {
                return usuario;
            }
        }
        return null;
    }

    public synchronized void registrar(Usuario nuevo) {
        for (Usuario usuario : usuarios) {
            if (usuario.getUsername().equalsIgnoreCase(nuevo.getUsername())
                    || usuario.getId().equals(nuevo.getId())) {
                throw new IllegalStateException("El usuario ya existe");
            }
        }
        usuarios.add(nuevo);
    }
}
