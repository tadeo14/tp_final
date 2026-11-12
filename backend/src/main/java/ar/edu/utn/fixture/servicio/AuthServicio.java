package ar.edu.utn.fixture.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.utn.fixture.dto.Pedidos.Credenciales;
import ar.edu.utn.fixture.dto.Pedidos.NuevoOrganizador;
import ar.edu.utn.fixture.dto.Respuestas.OrganizadorCreado;
import ar.edu.utn.fixture.dto.Respuestas.Sesion;
import ar.edu.utn.fixture.entidad.Organizador;
import ar.edu.utn.fixture.repositorio.OrganizadorRepositorio;
import ar.edu.utn.fixture.servicio.JwtServicio.TokenEmitido;

/**
 * Registro y login del organizador. Un solo rol: ORGANIZADOR (D44).
 */
@Service
@Transactional
public class AuthServicio {

    private final OrganizadorRepositorio organizadores;
    private final JwtServicio jwt;

    public AuthServicio(OrganizadorRepositorio organizadores, JwtServicio jwt) {
        this.organizadores = organizadores;
        this.jwt = jwt;
    }

    public OrganizadorCreado registrar(NuevoOrganizador pedido) {
        String email = normalizar(pedido.email());
        if (organizadores.findByEmail(email).isPresent()) {
            throw new ConflictoException("Ya existe un organizador con ese email");
        }
        Organizador o = new Organizador();
        o.setNombre(pedido.nombre());
        o.setEmail(email);
        o.setPasswordHash(Contrasenias.hashear(pedido.contrasenia()));
        organizadores.save(o);
        return new OrganizadorCreado(o.getId(), o.getNombre(), o.getEmail());
    }

    @Transactional(readOnly = true)
    public Sesion login(Credenciales credenciales) {
        Organizador o = organizadores.findByEmail(normalizar(credenciales.email()))
                .filter(encontrado -> Contrasenias.verificar(credenciales.contrasenia(), encontrado.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        TokenEmitido token = jwt.emitir(o);
        return new Sesion(token.token(), "Bearer", token.expiraEn(), new OrganizadorCreado(o.getId(), o.getNombre(), o.getEmail()));
    }

    /** El email se guarda y se busca en minúsculas y sin espacios: "Ana@Club.com " y "ana@club.com" son el mismo. */
    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
