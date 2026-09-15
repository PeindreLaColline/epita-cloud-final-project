package epita_cloud.com.backend.epita_cloud.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;

    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1시간

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
