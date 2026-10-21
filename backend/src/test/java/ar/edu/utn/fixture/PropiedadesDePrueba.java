package ar.edu.utn.fixture;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Propiedades que los tests de Spring necesitan además de la base (DB_URL…).
 * El secreto JWT se genera al azar en cada corrida: no hay ningún secreto guardado en el repo.
 */
public final class PropiedadesDePrueba {

    private static final String SECRETO = generarSecreto();

    private PropiedadesDePrueba() {
    }

    public static void registrar(DynamicPropertyRegistry registro) {
        registro.add("app.jwt.secreto", () -> SECRETO);
        registro.add("app.jwt.expiracion-min", () -> "60");
    }

    private static String generarSecreto() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
