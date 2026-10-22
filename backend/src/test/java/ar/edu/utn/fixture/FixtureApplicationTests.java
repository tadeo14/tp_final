package ar.edu.utn.fixture;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Test de contexto: arranca la aplicación completa contra la base configurada en DB_URL.
 * Pasa solo si Flyway aplica las migraciones y Hibernate valida todas las entidades contra las tablas.
 */
@SpringBootTest
class FixtureApplicationTests {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        PropiedadesDePrueba.registrar(registro);
    }

    @Test
    void contextLoads() {
    }
}
