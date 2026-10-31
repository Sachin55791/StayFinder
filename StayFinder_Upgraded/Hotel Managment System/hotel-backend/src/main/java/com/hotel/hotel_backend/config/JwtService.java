package com.hotel.hotel_backend.config;
import java.nio.charset.StandardCharsets;import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;import io.jsonwebtoken.security.Keys;
@Service public class JwtService {
 private final SecretKey key; private final long expiration;
 public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiration){this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiration=expiration;}
 public String generate(String email,String role){long now=System.currentTimeMillis();return Jwts.builder().subject(email).claim("role",role).issuedAt(new Date(now)).expiration(new Date(now+expiration)).signWith(key).compact();}
 public String extractEmail(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
 public String extractRole(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role",String.class);}
 public boolean isValid(String token){try{return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration().after(new Date());}catch(Exception e){return false;}}
}
