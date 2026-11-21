package ar.edu.utn.fixture.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContraseniasTest {

    @Test
    void verificar_conLaContraseniaCorrecta_esTrue() {
        String guardado = Contrasenias.hashear("clave-de-prueba");

        assertThat(Contrasenias.verificar("clave-de-prueba", guardado)).isTrue();
    }

    @Test
    void verificar_conOtraContrasenia_esFalse() {
        String guardado = Contrasenias.hashear("clave-de-prueba");

        assertThat(Contrasenias.verificar("clave-de-prueb", guardado)).isFalse();
    }

    @Test
    void laMismaContraseniaDaHashesDistintos_porLaSal() {
        assertThat(Contrasenias.hashear("clave-de-prueba")).isNotEqualTo(Contrasenias.hashear("clave-de-prueba"));
    }

    @Test
    void verificar_conAlgoQueNoEsUnHash_esFalse() {
        assertThat(Contrasenias.verificar("clave", "sin-login")).isFalse();
        assertThat(Contrasenias.verificar("clave", "pbkdf2$no-numero$xx$yy")).isFalse();
        assertThat(Contrasenias.verificar("clave", null)).isFalse();
    }
}
