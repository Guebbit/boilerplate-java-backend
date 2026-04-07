package com.guebbit.backend.security;

import com.guebbit.backend.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final AppProperties props;

    public JwtService(AppProperties props) {
        this.props = props;
    }

    public String createAccessToken(String userId, String email, boolean admin) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(props.security().issuer())
                .subject(userId)
                .claims(Map.of("email", email, "admin", admin, "typ", "access"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(props.tokens().accessTokenSeconds())))
                .signWith(accessKey())
                .compact();
    }

    public String createRefreshToken(String userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(props.security().issuer())
                .subject(userId)
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(props.tokens().refreshTokenSeconds())))
                .signWith(refreshKey())
                .compact();
    }

    public Claims parseAccess(String token) {
        return Jwts.parser().verifyWith(accessKey()).build().parseSignedClaims(token).getPayload();
    }

    public Claims parseRefresh(String token) {
        return Jwts.parser().verifyWith(refreshKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey accessKey() {
        return Keys.hmacShaKeyFor(props.security().accessTokenSecret().getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey refreshKey() {
        return Keys.hmacShaKeyFor(props.security().refreshTokenSecret().getBytes(StandardCharsets.UTF_8));
    }
}
