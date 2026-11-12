package ar.edu.utn.fixture.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.fixture.dto.Pedidos.Credenciales;
import ar.edu.utn.fixture.dto.Pedidos.NuevoOrganizador;
import ar.edu.utn.fixture.dto.Respuestas.OrganizadorCreado;
import ar.edu.utn.fixture.dto.Respuestas.Sesion;
import ar.edu.utn.fixture.servicio.AuthServicio;
import jakarta.validation.Valid;

/**
 * Registro y login del organizador. Son públicos: no piden token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final AuthServicio servicio;

    public AuthControlador(AuthServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizadorCreado registrar(@Valid @RequestBody NuevoOrganizador pedido) {
        return servicio.registrar(pedido);
    }

    @PostMapping("/login")
    public Sesion login(@Valid @RequestBody Credenciales credenciales) {
        return servicio.login(credenciales);
    }
}
