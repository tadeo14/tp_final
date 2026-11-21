package ar.edu.utn.fixture.servicio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import ar.edu.utn.fixture.entidad.Organizador;

/**
 * Emite y verifica los tokens JWT del login (D44).
 *
 * <p>El token lleva el id del organizador como "subject" y vence a los {@code JWT_EXPIRACION_MIN} minutos.
 * Se firma con HMAC-SHA256 y el secreto {@code JWT_SECRETO}: quien no conoce el secreto no puede fabricar
 * ni modificar un token. El secreto y la expiración salen de variables de entorno, nunca del código.
 */
@Service
public class JwtServicio {

    private static final String EMISOR = "fixture-basquet";
    private static final int LARGO_MINIMO_SECRETO = 32; // 256 bits, lo recomendado para HMAC-SHA256

    /** Token recién emitido y cuándo vence. */
    public record TokenEmitido(String token, Instant expiraEn) {
    }

    private final Algorithm algoritmo;
    private final JWTVerifier verificador;
    private final Duration expiracion;
    private final Clock reloj;

    @Autowired
    public JwtServicio(@Value("${app.jwt.secreto}") String secreto, @Value("${app.jwt.expiracion-min}") long expiracionMin) {
        this(secreto, expiracionMin, Clock.systemUTC());
    }

    /** El reloj se puede cambiar en los tests (por ejemplo, para emitir un token ya vencido). */
    JwtServicio(String secreto, long expiracionMin, Clock reloj) {
        if (secreto == null || secreto.length() < LARGO_MINIMO_SECRETO) {
            throw new IllegalStateException("JWT_SECRETO tiene que tener al menos " + LARGO_MINIMO_SECRETO + " caracteres");
        }
        if (expiracionMin <= 0) {
            throw new IllegalStateException("JWT_EXPIRACION_MIN tiene que ser mayor que 0");
        }
        this.algoritmo = Algorithm.HMAC256(secreto);
        this.verificador = JWT.require(algoritmo).withIssuer(EMISOR).build();
        this.expiracion = Duration.ofMinutes(expiracionMin);
        this.reloj = reloj;
    }

    public TokenEmitido emitir(Organizador organizador) {
        Instant ahora = reloj.instant();
        Instant vence = ahora.plus(expiracion);
        String token = JWT.create()
                .withIssuer(EMISOR)
                .withSubject(String.valueOf(organizador.getId()))
                .withIssuedAt(ahora)
                .withExpiresAt(vence)
                .sign(algoritmo);
        return new TokenEmitido(token, vence);
    }

    /** El id del organizador si el token es válido (firma correcta y no vencido); vacío en cualquier otro caso. */
    public Optional<Long> organizadorDe(String token) {
        try {
            return Optional.of(Long.valueOf(verificador.verify(token).getSubject()));
        } catch (JWTVerificationException | NumberFormatException e) {
            return Optional.empty();
        }
    }
}
