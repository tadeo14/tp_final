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

    /** Dominio del frontend de prueba habilitado por CORS. */
    public static final String ORIGEN_FRONTEND = "https://frontend.prueba.test";

    private PropiedadesDePrueba() {
    }

    public static void registrar(DynamicPropertyRegistry registro) {
        registro.add("app.jwt.secreto", () -> SECRETO);
        registro.add("app.jwt.expiracion-min", () -> "60");
        registro.add("app.cors.origenes", () -> ORIGEN_FRONTEND);
    }

    private static String generarSecreto() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
