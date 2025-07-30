package pillihuaman.com.pe.neuroIA;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuración de seguridad específica para el microservicio NeuroIA.
 * Define rutas públicas, rutas protegidas por roles y la configuración general.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita la seguridad a nivel de método (ej. @PreAuthorize)
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // El constructor ahora es más simple, solo necesita el filtro JWT.
    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Habilitamos CORS usando la configuración global de WebConfig.java
                .cors(withDefaults())

                // Definimos las reglas de autorización para las rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // Rutas públicas explícitas para este servicio
                                "/private/v1/ia/files/getCatalogImagen",

                                // Rutas que delegan la autenticación al servicio de seguridad
                                "/api/v1/auth/**",

                                // Rutas públicas para la documentación de la API
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll() // Permite el acceso a estas rutas sin autenticación.

                        // Define reglas específicas basadas en roles/autoridades
                        .requestMatchers("/private/**").hasAnyAuthority("USER", "ADMIN")

                        .anyRequest().authenticated() // CUALQUIER OTRA ruta requiere autenticación.
                )

                // Configuramos la gestión de sesiones para que sea SIN ESTADO
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Añadimos nuestro filtro JWT para validar tokens en cada petición
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Configuramos el logout
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }
}