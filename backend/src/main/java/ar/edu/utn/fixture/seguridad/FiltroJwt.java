package ar.edu.utn.fixture.seguridad;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import ar.edu.utn.fixture.servicio.JwtServicio;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee el encabezado "Authorization: Bearer {token}" de cada pedido. Si el token es válido, deja registrado
 * qué organizador hace el pedido (su id) con el único rol, ORGANIZADOR.
 *
 * <p>Si no hay token o no es válido, no corta el pedido: lo deja seguir sin autenticar. Las rutas públicas
 * responden igual, y las protegidas las rechaza SeguridadConfig con 401.
 */
class FiltroJwt extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwt;

    FiltroJwt(JwtServicio jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = pedido.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            jwt.organizadorDe(encabezado.substring(PREFIJO.length())).ifPresent(organizadorId -> {
                var autenticacion = new UsernamePasswordAuthenticationToken(organizadorId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + SeguridadConfig.ROL)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        cadena.doFilter(pedido, respuesta);
    }
}
