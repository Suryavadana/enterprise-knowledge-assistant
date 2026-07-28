package com.example.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.server.entity.Document;
import com.example.server.entity.User;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserOrderByUploadedAtDesc(User user);
}
