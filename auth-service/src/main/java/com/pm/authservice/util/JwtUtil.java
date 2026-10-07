package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey secretKey;
  private final Duration expiration;

  public JwtUtil(@Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration}") Duration expiration) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.expiration = expiration;
  }

  public String generateToken(String email, String role) {
    Date now = new Date();
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expiration.toMillis()))
        .signWith(secretKey)
        .compact();
  }

  public void validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);
    } catch (SignatureException e) {
      throw new JwtException("Invalid JWT signature");
    } catch (JwtException e) {
      throw new JwtException("Invalid JWT");
    }
  }
}
