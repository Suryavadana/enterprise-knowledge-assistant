package com.example.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.server.entity.PromptTemplate;
import com.example.server.entity.User;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    List<PromptTemplate> findByUserOrderByCreatedAtDesc(User user);
}
