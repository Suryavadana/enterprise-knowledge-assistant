package com.example.server.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.server.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000;

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(user.getEmail()) //sets the JWT's standard sub claim to the user's email. This is "whose token is this."
                .claim("role", user.getRole().name()) //adds a custom claim (not a standard JWT field, but you can add any key-value pairs you want). .name() converts the Role enum (ADMIN/USER) into its string form, since JWT claims are just JSON — no enums allowed directly.
                .issuedAt(issuedAt) //standard iat and exp claims, in Unix-time-like format
                .expiration(expiresAt) //signs everything above with your secret key using HMAC-SHA256
                .signWith(signingKey, Jwts.SIG.HS256) //serializes the whole thing into the final header.payload.signature string format
                .compact();
    }

    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
