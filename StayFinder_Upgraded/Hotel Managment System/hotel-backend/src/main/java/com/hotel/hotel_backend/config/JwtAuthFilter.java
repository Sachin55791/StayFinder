package com.hotel.hotel_backend.config;
import java.io.IOException;import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;import org.springframework.security.core.authority.SimpleGrantedAuthority;import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;import jakarta.servlet.FilterChain;import jakarta.servlet.ServletException;import jakarta.servlet.http.*;
@Component public class JwtAuthFilter extends OncePerRequestFilter {
 private final JwtService jwt; public JwtAuthFilter(JwtService jwt){this.jwt=jwt;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String h=req.getHeader("Authorization");if(h!=null&&h.startsWith("Bearer ")){String token=h.substring(7);try{if(jwt.isValid(token)){String email=jwt.extractEmail(token);String role=jwt.extractRole(token);var auth=new UsernamePasswordAuthenticationToken(email,null,java.util.List.of(new SimpleGrantedAuthority("ROLE_"+role)));SecurityContextHolder.getContext().setAuthentication(auth);}}catch(Exception ignored){}}
  chain.doFilter(req,res);
 }
}
