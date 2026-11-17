package model;

public abstract class Usuario {
    private final String id;
    private final String username;
    private final String password;
    private final Rol rol;

    protected Usuario(String id, String username, String password, Rol rol) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El id es obligatorio");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("El usuario es obligatorio");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("La contraseña es obligatoria");
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public Rol getRol() { return rol; }

    /** Comparación básica para el ejercicio; el almacenamiento real debe guardar contraseñas con hash. */
    public boolean autenticar(String passwordIngresada) {
        return password.equals(passwordIngresada);
    }
}
