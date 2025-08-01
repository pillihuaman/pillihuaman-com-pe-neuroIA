package pillihuaman.com.pe.neuroIA;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class FakeUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Carga desde tu JwtService (suponiendo que tienes acceso al token actual)
        List<String> roles = Arrays.asList("USER", "ADMIN"); // o desde el JWT

        return User.builder()
                .username(username)
                .password("")
                .authorities(roles.stream().map(SimpleGrantedAuthority::new).toList())
                .build();
    }
}