package com.example.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.server.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    //by extending this interface, you automatically get a full set of database operations for free — save(), findById(), findAll(), deleteById(), etc.

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
