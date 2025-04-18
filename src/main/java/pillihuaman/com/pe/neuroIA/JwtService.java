package pillihuaman.com.pe.neuroIA;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.ResponseUser;
import pillihuaman.com.pe.neuroIA.foreing.ExternalApiService;

import java.util.Map;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;
    private final ExternalApiService externalApiService;


    public JwtService(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token) // Remove "Bearer " prefix
                .getBody();

    }

    public MyJsonWebToken parseTokenToMyJsonWebToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = parseToken(token);
        MyJsonWebToken myJsonWebToken = new MyJsonWebToken();
        Map<String, Object> userMap = (Map<String, Object>) claims.get("user");
        if (userMap != null) {
            ResponseUser user = new ResponseUser();
            user.setId(new ObjectId(userMap.get("id").toString()));
            user.setMail(userMap.get("email").toString());
            myJsonWebToken.setUser(user);
        }
        return myJsonWebToken;
    }

    public boolean isTokenValid(String token) {
        return externalApiService.isTokenValid(token);
    }
}
