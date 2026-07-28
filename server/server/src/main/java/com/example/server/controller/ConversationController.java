package com.example.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.server.dto.ConversationSummary;
import com.example.server.dto.MessageDto;
import com.example.server.entity.Conversation;
import com.example.server.entity.User;
import com.example.server.repository.ConversationRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.CurrentUserService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final CurrentUserService currentUserService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationController(
            CurrentUserService currentUserService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        this.currentUserService = currentUserService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConversationSummary>> listConversations() {
        User user = currentUserService.getCurrentUser();

        List<ConversationSummary> summaries = conversationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(conversation -> new ConversationSummary(
                        conversation.getId(), conversation.getTitle(), conversation.getCreatedAt()))
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDto>> listMessages(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        Conversation conversation = loadOwnedConversation(id, user);

        List<MessageDto> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(message -> new MessageDto(
                        message.getId(), message.getRole().name(), message.getContent(), message.getCreatedAt()))
                .toList();

        return ResponseEntity.ok(messages);
    }

    private Conversation loadOwnedConversation(Long conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this conversation");
        }

        return conversation;
    }
}
