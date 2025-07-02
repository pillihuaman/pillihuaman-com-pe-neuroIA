    package pillihuaman.com.pe.neuroIA;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

    import java.util.Arrays;
    import java.util.List;

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    public class SecurityConfiguration {
        private final JwtAuthenticationFilter jwtAuthFilter;
        private final CorsProperties corsProperties;

        public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter, CorsProperties corsProperties) {
            this.jwtAuthFilter = jwtAuthFilter;
            this.corsProperties = corsProperties;
        }        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth

                            .requestMatchers(
                                    "/private/v1/ia/files/getCatalogImagen",
                                    "/api/v1/auth/**",  // Public Auth Endpoints
                                    "/swagger-ui/**",   // Swagger UI
                                    "/v3/api-docs/**"   // API Docs
                            ).permitAll()
                           .requestMatchers("/private/**").hasAnyAuthority("USER", "ADMIN")
                            .anyRequest().authenticated()
                    )
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .logout(logout -> logout
                            .logoutUrl("/api/v1/auth/logout")
                            .logoutSuccessHandler((request, response, authentication) ->
                                    SecurityContextHolder.clearContext())
                    );

            return http.build();
        }
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(corsProperties.getAllowedOrigins());
            config.setAllowedMethods(corsProperties.getAllowedMethods());
            config.setAllowedHeaders(corsProperties.getAllowedHeaders());
            config.setAllowCredentials(corsProperties.isAllowCredentials());

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }
    }
