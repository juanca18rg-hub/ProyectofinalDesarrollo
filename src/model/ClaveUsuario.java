package model;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Guarda lo necesario para verificar una contraseña al iniciar sesión. */
public final class ClaveUsuario {
    private static final SecureRandom AZAR = new SecureRandom();
    private final int iteraciones;
    private final String sal;
    private final String hash;

    public ClaveUsuario(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        byte[] salAleatoria = new byte[16];
        AZAR.nextBytes(salAleatoria);
        iteraciones = 600_000;
        sal = Base64.getEncoder().encodeToString(salAleatoria);
        hash = Base64.getEncoder().encodeToString(calcular(password));
    }

    public boolean coincide(String intento) {
        return intento != null && MessageDigest.isEqual(
                Base64.getDecoder().decode(hash), calcular(intento));
    }

    void validar() {
        if (iteraciones <= 0 || sal == null || hash == null
                || Base64.getDecoder().decode(sal).length != 16
                || Base64.getDecoder().decode(hash).length != 32) {
            throw new IllegalArgumentException("La credencial guardada está incompleta");
        }
    }

    private byte[] calcular(String password) {
        PBEKeySpec datos = new PBEKeySpec(password.toCharArray(),
                Base64.getDecoder().decode(sal), iteraciones, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(datos).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo verificar la contraseña", e);
        } finally {
            datos.clearPassword();
        }
    }
}
