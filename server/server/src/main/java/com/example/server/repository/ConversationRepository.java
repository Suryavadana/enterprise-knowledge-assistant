package com.example.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.server.entity.Conversation;
import com.example.server.entity.User;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByCreatedAtDesc(User user);
}
