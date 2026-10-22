package ar.edu.utn.fixture.servicio;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
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
        byte[] hash = derivar(contrasenia, sal, ITERACIONES, BITS);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /**
     * Recalcula el hash de la contraseña con la sal y las iteraciones guardadas, y lo compara con el guardado.
     * Devuelve false si no coincide o si lo guardado no tiene el formato esperado.
     */
    static boolean verificar(String contrasenia, String guardado) {
        if (contrasenia == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(contrasenia, sal, iteraciones, esperado.length * 8);
            // Comparación en tiempo constante: no revela cuántos bytes coinciden.
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String contrasenia, byte[] sal, int iteraciones, int bits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasenia.toCharArray(), sal, iteraciones, bits);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
