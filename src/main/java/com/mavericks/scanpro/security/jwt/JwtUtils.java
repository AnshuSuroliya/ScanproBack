package com.mavericks.scanpro.security.jwt;

import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.security.UserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    @Autowired
    private UserRepo userRepository;

    @Value("${MaverickBank.app.JwtSecret}")
    private String jwtSecret="itsasecretboiiiiiiijfasdkhfadjskfheuafhldskfjheiowfjdklsfeaiwfhdsmfdsj";


    private int jwtExpirationMs=8640000;

    public String generateJwtToken(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .setClaims(new HashMap<String,String>(){{
                    put("email",userRepository.findById(userPrincipal.getId()).get().getEmail());
                    put("Id",userPrincipal.getId().toString());
                    put("Role",userPrincipal.getAuthorities().stream().findFirst().get().toString());
                    put("Fullname",userRepository.findById(userPrincipal.getId()).get().getFullname());
                }})
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {

        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().get("email").toString();
    }
    public Object getClaimsFromJwtToken(String token) {

        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }


    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}