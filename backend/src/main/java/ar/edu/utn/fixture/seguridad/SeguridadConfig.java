package ar.edu.utn.fixture.seguridad;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.edu.utn.fixture.dto.Respuestas.Error;
import ar.edu.utn.fixture.servicio.JwtServicio;

/**
 * Qué rutas son públicas y cuáles exigen un organizador autenticado (D44).
 *
 * <ul>
 *   <li>Públicas: login y registro, vista pública de torneos publicados y /api/salud.</li>
 *   <li>Todo lo demás exige un token válido del rol ORGANIZADOR; si falta, 401.</li>
 * </ul>
 * Sin sesiones en el servidor (cada pedido trae su token) y sin CSRF, porque el token viaja en un
 * encabezado y no en una cookie.
 */
@Configuration
public class SeguridadConfig {

    /** Único rol de la aplicación. */
    static final String ROL = "ORGANIZADOR";

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtServicio jwt, ObjectMapper json,
            @Value("${app.cors.origenes}") String origenesCors) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(configuracionCors(origenesCors)))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // consultas previas de CORS
                        .requestMatchers("/api/auth/**", "/api/publico/**", "/api/salud", "/error").permitAll()
                        .anyRequest().hasRole(ROL))
                .exceptionHandling(e -> e.authenticationEntryPoint(respuesta401(json)))
                .addFilterBefore(new FiltroJwt(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Sin token válido en una ruta protegida: 401 con el mismo formato de error que el resto de la API. */
    private static AuthenticationEntryPoint respuesta401(ObjectMapper json) {
        return (pedido, respuesta, excepcion) -> {
            respuesta.setStatus(HttpStatus.UNAUTHORIZED.value());
            respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
            respuesta.setCharacterEncoding("UTF-8");
            json.writeValue(respuesta.getOutputStream(),
                    new Error("Hay que iniciar sesión: falta el token o no es válido", List.of()));
        };
    }

    /** Dominios del frontend que pueden llamar a la API, de la variable de entorno CORS_ORIGENES (separados por coma). */
    private static CorsConfigurationSource configuracionCors(String origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }
}
