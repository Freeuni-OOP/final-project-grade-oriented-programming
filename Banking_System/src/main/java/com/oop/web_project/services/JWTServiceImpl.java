package com.oop.web_project.services;

import com.oop.web_project.exceptions.customerExceptions.CustomerCannotBeAuthenticatedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.internal.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTServiceImpl implements JWTService {

    @Value("${security.jwt_secret_key}")
    private String SECRET_KEY;

    @Value("${security.jwt_expiration_time}")
    private Long expiration;

    @Override
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        String generatedToken = null;
        try {
            generatedToken = Jwts.builder()
                    .addClaims(claims)
                    .setSubject(email)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getKey())
                    .compact();
        } catch (Exception e) {
            throw new JwtException("Cannot built JWT token!");
        }
        return generatedToken;
    }

    @Override
    public String extractEmail(String jwtToken) {

        String extracted = extractClaim
                (jwtToken, Claims::getSubject);

        if(extracted == null) {
            throw new JwtException("jwt token contained no email!");
        }

        return extracted;
    }

    @Override
    public Boolean validateToken(String jwtToken, UserDetails userDetails) {
        String email = extractEmail(jwtToken);

        return email.equals(userDetails.getUsername())
                && !isTokenExpired(jwtToken);
    }

    /**
     * Method checks whether JWT token is expired or not.
     * JWT token is expired, it looks at segment of the jwt token(claim), which checks jwt expiration date. if token's
     * expiration date is before current date, then JWT Token is expired.
     * @param jwtToken token to be checked for expiration
     * @return true, if JWT token is expired, false otherwise
     */
    private Boolean isTokenExpired(String jwtToken) {
        return extractClaim(jwtToken, Claims::getExpiration)
                .before(new Date());
    }

    /**
     * Extracts particular claim out of JWT token.
     * Method is generic, because claim could be presented as many types, as it is generic information. It may contain
     * expiration date, or URL, or list of values and so on
     * @param jwtToken JWT token, from which we are taking out the claim
     * @param claimResolver Functional interface that allows us to take out particular claim out of claims
     * @return extracted claim
     */
    private <T> T extractClaim(String jwtToken, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimResolver.apply(claims);
    }

    /**
     * Extracts all claims present in JWT token
     * @param jwtToken token from which claims must be extracted out
     * @return claims present in JWT token
     */
    private Claims extractAllClaims(String jwtToken) {
        Claims claims = null;

        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();
        } catch(Exception e) {
            throw new CustomerCannotBeAuthenticatedException("JWT Token validation failed!");
        }
        return claims;
    }

    /**
     * Method decodes secret key and returns it
     * @return SecretKey implementation instance
     */
    private SecretKey getKey() {
        byte[] keyValue = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyValue);
    }

}
