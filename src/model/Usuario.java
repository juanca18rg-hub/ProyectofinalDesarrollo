package model;

public abstract class Usuario {
    private final String id;
    private final String username;
    private final ClaveUsuario clave;
    private final Rol rol;

    protected Usuario(String id, String username, String password, Rol rol) {
        this.id = id;
        this.username = username;
        this.clave = new ClaveUsuario(password);
        this.rol = rol;
        validar();
    }

    public void validar() {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("El id es obligatorio");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("El usuario es obligatorio");
        if (rol == null || clave == null) throw new IllegalArgumentException("La cuenta está incompleta");
        clave.validar();
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public Rol getRol() { return rol; }

    public boolean autenticar(String passwordIngresada) {
        return clave.coincide(passwordIngresada);
    }
}
