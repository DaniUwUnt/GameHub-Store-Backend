package cl.gamehub.ordenes.config;

import cl.gamehub.comun.seguridad.JwtServicio;
import cl.gamehub.comun.seguridad.SeguridadBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** El visitante valida su carrito; comprar exige rol CLIENTE; la bandeja, VENDEDOR o ADMIN. */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtServicio jwtServicio,
                                               @Value("${app.clave-interna}") String claveInterna) throws Exception {
        SeguridadBase.aplicar(http, jwtServicio, claveInterna);
        http.authorizeHttpRequests(reglas -> {
            SeguridadBase.rutasAbiertas(reglas);
            reglas.requestMatchers(HttpMethod.POST, "/api/v1/carrito/validar").permitAll()
                  .requestMatchers("/api/v1/ordenes/**").hasRole("CLIENTE")
                  .requestMatchers(HttpMethod.GET, "/api/v1/operador/ordenes", "/api/v1/operador/ordenes/*")
                      .hasAnyRole("VENDEDOR", "ADMIN")
                  .requestMatchers("/api/v1/operador/**").hasRole("ADMIN")
                  .anyRequest().authenticated();
        });
        return http.build();
    }
}
