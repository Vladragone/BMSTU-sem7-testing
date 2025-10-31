package com.example.game.util;

import com.example.game.service.interfaces.ITokenParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

@Component
public class JwtParserUtil implements ITokenParser {

    private static final String SECRET_KEY = "YourSecretKeyForJWTGenerationDontShareThis";

    @Override
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    private Claims parseToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
