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
                .csrf(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS: sin autenticación
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 2. RUTAS PRIVADAS ESPECÍFICAS: Las más específicas primero.
                        // Esta ruta de subida necesita un rol de servicio o de admin.
                        .requestMatchers("/private/v1/ia/files/upload").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers("/private/v1/ia/files/delete/**").hasAnyAuthority("ADMIN", "USER")

                        // 3. RUTAS PRIVADAS GENERALES: Cualquier usuario autenticado puede acceder.
                        // Esta regla es ahora más permisiva y se aplica a lo que no coincidió antes.
                        .requestMatchers("/private/**").authenticated()

                        // 4. FALLBACK: Cualquier otra ruta no definida requiere autenticación.
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }
}