package com.example.server;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.server.security.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //this is what http.cors() below actually consults - it replaces the old WebMvcConfigurer-based
    //CorsConfig, which only ran inside Spring MVC's dispatch and never got a chance to run on requests
    //Spring Security rejected first (every preflight under anyRequest().authenticated())
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    //a chain of security checks every incoming request passes through before reaching your controller. Right now there's an invisible default chain blocking everything; we're replacing it with our own rules.
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                //CSRF disabled — CSRF protection matters for browser-based forms with cookies/sessions. Since React app calls the API directly with JSON and (soon) a JWT in a header, this attack vector doesn't apply the same way — disabling it here is standard for stateless JSON APIs, not a security shortcut.
                .csrf(csrf -> csrf.disable())
                //wires the CorsConfigurationSource bean above into the security filter chain itself, so CORS
                //handling (including the Access-Control-Allow-* response headers) runs before the authorization
                //rules below get a chance to reject anything
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //STATELESS session policy — tells Spring "don't create or use HttpSession at all." Every request must prove who it is on its own (via the JWT, once we add it) — the server holds no memory of "being logged in" between requests. This is the foundational shift from traditional session-based auth to JWT-based auth.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //preflight OPTIONS requests never carry the Authorization header (browsers deliberately
                        //send them "bare" to ask permission first) - anyRequest().authenticated() would 401 every
                        //one of them before the browser ever gets to send the real request, which is the CORS
                        //failure being fixed here
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
                        //TEMPORARY - remove this line along with TestController once GeminiService is confirmed working
                        .requestMatchers("/api/test/gemini").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
