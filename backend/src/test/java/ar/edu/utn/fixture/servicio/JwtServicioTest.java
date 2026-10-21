package ar.edu.utn.fixture.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import ar.edu.utn.fixture.entidad.Organizador;

/**
 * Tokens JWT. El secreto es de PRUEBA y solo existe en este test.
 */
class JwtServicioTest {

    private static final String SECRETO = "secreto-de-prueba-de-al-menos-32-caracteres";

    @Test
    void unTokenEmitidoSeVerificaYDevuelveElOrganizador() {
        JwtServicio jwt = new JwtServicio(SECRETO, 60);

        String token = jwt.emitir(organizador(7L)).token();

        assertThat(jwt.organizadorDe(token)).contains(7L);
    }

    @Test
    void unTokenVencido_noEsValido() {
        Clock haceDosHoras = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String vencido = new JwtServicio(SECRETO, 60, haceDosHoras).emitir(organizador(7L)).token();

        assertThat(new JwtServicio(SECRETO, 60).organizadorDe(vencido)).isEmpty();
    }

    @Test
    void unTokenFirmadoConOtroSecreto_noEsValido() {
        String ajeno = new JwtServicio("otro-secreto-de-prueba-de-32-caracteres!!", 60).emitir(organizador(7L)).token();

        assertThat(new JwtServicio(SECRETO, 60).organizadorDe(ajeno)).isEmpty();
    }

    @Test
    void unTextoCualquiera_noEsValido() {
        assertThat(new JwtServicio(SECRETO, 60).organizadorDe("no-es-un-token")).isEmpty();
    }

    @Test
    void rechazaUnSecretoCortoOUnaExpiracionInvalida() {
        assertThatThrownBy(() -> new JwtServicio("corto", 60)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtServicio(SECRETO, 0)).isInstanceOf(IllegalStateException.class);
    }

    private static Organizador organizador(long id) {
        Organizador o = new Organizador() {
            @Override
            public Long getId() {
                return id;
            }
        };
        return o;
    }
}
