package com.example.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.server.dto.PromptTemplateRequest;
import com.example.server.dto.PromptTemplateResponse;
import com.example.server.entity.PromptTemplate;
import com.example.server.entity.User;
import com.example.server.repository.PromptTemplateRepository;
import com.example.server.service.CurrentUserService;

@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {

    private final CurrentUserService currentUserService;
    private final PromptTemplateRepository promptTemplateRepository;

    public PromptTemplateController(
            CurrentUserService currentUserService, PromptTemplateRepository promptTemplateRepository) {
        this.currentUserService = currentUserService;
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @GetMapping
    public ResponseEntity<List<PromptTemplateResponse>> listPromptTemplates() {
        User user = currentUserService.getCurrentUser();

        List<PromptTemplateResponse> responses = promptTemplateRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<PromptTemplateResponse> createPromptTemplate(@RequestBody PromptTemplateRequest request) {
        User user = currentUserService.getCurrentUser();

        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setUser(user);
        promptTemplate.setTitle(request.title());
        promptTemplate.setContent(request.content());

        PromptTemplate saved = promptTemplateRepository.save(promptTemplate);

        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptTemplateResponse> updatePromptTemplate(
            @PathVariable Long id, @RequestBody PromptTemplateRequest request) {
        User user = currentUserService.getCurrentUser();
        PromptTemplate promptTemplate = loadOwnedPromptTemplate(id, user);

        promptTemplate.setTitle(request.title());
        promptTemplate.setContent(request.content());

        PromptTemplate saved = promptTemplateRepository.save(promptTemplate);

        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromptTemplate(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        PromptTemplate promptTemplate = loadOwnedPromptTemplate(id, user);

        promptTemplateRepository.delete(promptTemplate);

        return ResponseEntity.noContent().build();
    }

    private PromptTemplate loadOwnedPromptTemplate(Long id, User user) {
        PromptTemplate promptTemplate = promptTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prompt template not found"));

        if (!promptTemplate.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this prompt template");
        }

        return promptTemplate;
    }

    private PromptTemplateResponse toResponse(PromptTemplate promptTemplate) {
        return new PromptTemplateResponse(
                promptTemplate.getId(),
                promptTemplate.getTitle(),
                promptTemplate.getContent(),
                promptTemplate.getCreatedAt());
    }
}
