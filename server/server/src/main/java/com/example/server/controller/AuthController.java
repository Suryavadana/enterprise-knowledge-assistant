package com.example.server.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.server.dto.LoginRequest;
import com.example.server.dto.LoginResponse;
import com.example.server.dto.SignupRequest;
import com.example.server.dto.UserResponse;
import com.example.server.entity.PromptTemplate;
import com.example.server.entity.User;
import com.example.server.repository.PromptTemplateRepository;
import com.example.server.repository.UserRepository;
import com.example.server.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PromptTemplateRepository promptTemplateRepository;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PromptTemplateRepository promptTemplateRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        //Null/blank validation before hitting the database — checking request.email() and request.password() upfront avoids wasted DB calls and null pointer risk. (Notice request.email() — no get prefix — that tells me SignupRequest is a Java record, a compact way to define a DTO. Good sign it kept entity vs. request DTOs separate, like we asked.
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
//✅ existsByEmail() check before creating — correct order: check first, reject with 409 Conflict if taken, otherwise proceed.
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        //Password hashing — passwordEncoder.encode(request.password()) happens before setPassword(), so the raw password is never persisted. This is the most important line in the whole file to get right, and it's right.
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        seedDefaultPromptTemplates(saved);
//UserResponse.from(saved) — good sign it's converting the saved User entity into a separate response DTO rather than returning the entity directly (which would risk accidentally serializing the password hash back to the client).
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(saved));
    }

    private void seedDefaultPromptTemplates(User user) {
        promptTemplateRepository.save(
                newPromptTemplate(user, "Summarize document", "Please summarize the following in 3 bullet points:"));
        promptTemplateRepository.save(newPromptTemplate(
                user,
                "Explain like I'm new to this",
                "Explain the following concept in simple terms, as if to someone new to the topic:"));
        promptTemplateRepository.save(newPromptTemplate(
                user,
                "Generate test cases",
                "Generate a list of test cases (including edge cases) for the following:"));
    }

    private PromptTemplate newPromptTemplate(User user, String title, String content) {
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setUser(user);
        promptTemplate.setTitle(title);
        promptTemplate.setContent(content);
        return promptTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> found = userRepository.findByEmail(request.email());

        if (found.isEmpty() || !passwordEncoder.matches(request.password(), found.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        User user = found.get();
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(token, UserResponse.from(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<String> me() {
        return ResponseEntity.ok("You are authenticated");
    }
}
