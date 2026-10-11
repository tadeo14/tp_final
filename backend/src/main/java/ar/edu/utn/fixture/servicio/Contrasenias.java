package ar.edu.utn.fixture.servicio;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Guarda las contraseñas como hash PBKDF2 con sal aleatoria, usando solo la biblioteca estándar de Java
 * (sin dependencias nuevas). Nunca se guarda la contraseña en texto plano.
 * Formato guardado: pbkdf2$iteraciones$sal$hash (sal y hash en Base64).
 */
final class Contrasenias {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 210_000;
    private static final int BITS = 256;
    private static final SecureRandom AZAR = new SecureRandom();

    private Contrasenias() {
    }

    static String hashear(String contrasenia) {
        byte[] sal = new byte[16];
        AZAR.nextBytes(sal);
        byte[] hash = derivar(contrasenia, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    private static byte[] derivar(String contrasenia, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasenia.toCharArray(), sal, iteraciones, BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
