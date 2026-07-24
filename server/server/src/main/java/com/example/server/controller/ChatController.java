package com.example.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.server.dto.ChatRequest;
import com.example.server.dto.ChatResponse;
import com.example.server.dto.GeminiMessage;
import com.example.server.entity.Conversation;
import com.example.server.entity.Message;
import com.example.server.entity.User;
import com.example.server.repository.ConversationRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.CurrentUserService;
import com.example.server.service.GeminiService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final CurrentUserService currentUserService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GeminiService geminiService;

    public ChatController(
            CurrentUserService currentUserService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GeminiService geminiService) {
        this.currentUserService = currentUserService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        User user = currentUserService.getCurrentUser();
        Conversation conversation = resolveConversation(request.conversationId(), user);

        Message userMessage = new Message();
        userMessage.setConversation(conversation);
        userMessage.setRole(Message.Role.USER);
        userMessage.setContent(request.message());
        messageRepository.save(userMessage);

        List<GeminiMessage> history = messageRepository.findByConversationOrderByCreatedAtAsc(conversation).stream()
                .map(message -> new GeminiMessage(toGeminiRole(message.getRole()), message.getContent()))
                .toList();

        String reply = geminiService.generateReply(history);

        Message assistantMessage = new Message();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole(Message.Role.ASSISTANT);
        assistantMessage.setContent(reply);
        messageRepository.save(assistantMessage);

        return ResponseEntity.ok(new ChatResponse(conversation.getId(), reply));
    }

    private Conversation resolveConversation(Long conversationId, User user) {
        if (conversationId == null) {
            Conversation conversation = new Conversation();
            conversation.setUser(user);
            return conversationRepository.save(conversation);
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this conversation");
        }

        return conversation;
    }

    private String toGeminiRole(Message.Role role) {
        return role == Message.Role.USER ? "user" : "model";
    }
}
