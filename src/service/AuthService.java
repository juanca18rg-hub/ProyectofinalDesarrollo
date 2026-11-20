package service;

import model.Administrador;
import model.Rol;
import model.Usuario;
import repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    private final UsuarioRepository usuarios;
    private final Map<String, Usuario> sesiones = new HashMap<>();

    public AuthService(String claveAdmin) {
        usuarios = new UsuarioRepository();
        prepararAdministrador(claveAdmin);
    }

    private void prepararAdministrador(String claveConfigurada) {
        Usuario existente = usuarios.buscarPorUsername("admin").orElse(null);
        if (existente != null) {
            if (existente.getRol() != Rol.ADMIN) {
                throw new IllegalStateException("El nombre admin pertenece a una cuenta de cliente");
            }
            return;
        }

        boolean generarClave = claveConfigurada == null || claveConfigurada.isBlank();
        String clave = generarClave ? UUID.randomUUID().toString() : claveConfigurada;
        usuarios.guardar(new Administrador("admin", "admin", clave));
        if (generarClave) {
            System.out.println("Clave inicial de admin (solo en este primer arranque): " + clave);
        }
    }

    public synchronized String iniciarSesion(String username, String password) {
        Usuario usuario = usuarios.buscarPorUsername(username).orElse(null);
        if (usuario == null || !usuario.autenticar(password)) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        sesiones.put(token, usuario);
        return token;
    }

    public synchronized Usuario buscarSesion(String token) {
        return sesiones.get(token);
    }

    public Usuario buscarPorId(String id) {
        return usuarios.buscarPorId(id).orElse(null);
    }

    public void registrar(Usuario nuevo) {
        usuarios.guardar(nuevo);
    }
}
