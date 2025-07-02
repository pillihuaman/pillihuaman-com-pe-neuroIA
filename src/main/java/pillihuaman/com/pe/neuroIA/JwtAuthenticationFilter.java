package pillihuaman.com.pe.neuroIA;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pillihuaman.com.pe.neuroIA.foreing.ExternalApiService;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private ExternalApiService externalApiService; // Calls Security MS

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader;

        if (!externalApiService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid token");
            return;
        }

        // Decode token and extract roles
        List<String> roles = extractRolesFromToken(token);

        // Validate ANONYMOUS access to restricted paths
        String path = request.getRequestURI();
        List<String> allowedForAnonymous = List.of(
                "/private/v1/ia/files/getCatalogImagen"
        );

        if (!allowedForAnonymous.contains(path)) {
            if (roles.contains("ANONYMOUS") && roles.size() == 1) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Forbidden: ANONYMOUS users cannot access this resource");
                return;
            }
        }

        // Convert roles to Spring authorities
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        // Set authentication context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(token, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
    private List<String> extractRolesFromToken(String token) {
        try {
            String[] parts = token.replace("Bearer ", "").split("\\.");
            if (parts.length < 2) return Collections.emptyList();

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1])); // Usa getUrlDecoder
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            Object rolesObj = claims.get("role");
            if (rolesObj instanceof List<?> roleList) {
                List<String> roleNames = new ArrayList<>();
                for (Object roleObj : roleList) {
                    if (roleObj instanceof Map<?, ?> roleMap && roleMap.containsKey("name")) {
                        roleNames.add(roleMap.get("name").toString());
                    }
                }
                return roleNames;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Opcional
        }
        return Collections.emptyList();
    }

}
