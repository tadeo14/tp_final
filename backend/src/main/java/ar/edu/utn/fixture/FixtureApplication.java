package ar.edu.utn.fixture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Se excluye el usuario en memoria que Spring Security crea por defecto: el login es propio, con JWT (D44).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FixtureApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixtureApplication.class, args);
    }
}
