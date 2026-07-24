package com.example.server.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.server.entity.User;
import com.example.server.repository.UserRepository;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //JwtAuthFilter already ran earlier in the chain for this request and, on a valid token, put an
    //Authentication into SecurityContextHolder - a ThreadLocal, so it's scoped to this request's thread
    //and safe to read back here without re-parsing the JWT. getName() returns the JWT subject (the email),
    //same value the filter passed into UsernamePasswordAuthenticationToken as the principal.
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
