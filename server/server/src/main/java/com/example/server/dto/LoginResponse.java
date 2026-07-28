package com.example.server.dto;

public record LoginResponse(String token, UserResponse user) {
}
