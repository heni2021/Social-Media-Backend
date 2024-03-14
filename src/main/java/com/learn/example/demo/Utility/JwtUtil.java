package com.learn.example.demo.Utility;

import com.learn.example.demo.Constants.iChatApplicationConstants;
import com.learn.example.demo.iChatApplication;
import com.learn.example.demo.Models.LoginModels.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(iChatApplication.class);

//    @Value("${jwt.secretKey}")
//    private String secretKey;
    private String secretKey = iChatApplicationConstants.SECRET_KEY_AUTH_TOKEN;

    private SecretKey key = null;


//    @PostConstruct
//    public void init() {
//        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
//        log.info("New key generated and saved successfully! - "+key);
//    }
    @PostConstruct
    public void init() {
        if (this.key == null) {
            synchronized (this) {
                if (this.key == null) {
                    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
                    log.info("New key generated and saved successfully! - " + key);
                }
            }
        }
    }


    // Generate token for user
//    public String generateToken(User userDetails) {
////        log.info("Key: "+key.toString());
//        Map<String, Object> claims = new HashMap<>();
//        log.info("Issuing token");
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(userDetails.getId())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
////                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                .signWith(SignatureAlgorithm.HS512, key)
//                .compact();
//    }

// Generate token for user
    public synchronized String generateToken(User userDetails) {
        Map<String, Object> claims = new HashMap<>();
        log.info("Issuing token");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    // Extract username from token
    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        log.info("Extracting Claims");
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract information from token using claims
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Checking Expirations");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        log.info("Extracting expiration time");
        return extractExpiration(token).before(new Date());
    }

    public String fetchId(String token){
        log.info("Fetching id!");
        final String _id = extractId(token);
        return _id;
    }

    // Validate token for a user
    public Boolean validateToken(String token, String id) {
        log.info("Fetching id!");
        final String _id = extractId(token);
        log.info("Comparing ids!");
//        return (id.equals(userDetails.getId()) && !isTokenExpired(token));
        return (_id.equals(id));
    }
}
