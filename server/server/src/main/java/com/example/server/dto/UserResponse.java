package com.example.server.dto;

import com.example.server.entity.User;

public record UserResponse(Long id, String email, String name, User.Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
