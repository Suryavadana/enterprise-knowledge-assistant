package com.example.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.server.dto.FeedbackRequest;
import com.example.server.dto.FeedbackResponse;
import com.example.server.entity.Feedback;
import com.example.server.entity.Message;
import com.example.server.entity.User;
import com.example.server.repository.FeedbackRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.CurrentUserService;

@RestController
@RequestMapping("/api/messages")
public class FeedbackController {

    private final CurrentUserService currentUserService;
    private final MessageRepository messageRepository;
    private final FeedbackRepository feedbackRepository;

    public FeedbackController(
            CurrentUserService currentUserService,
            MessageRepository messageRepository,
            FeedbackRepository feedbackRepository) {
        this.currentUserService = currentUserService;
        this.messageRepository = messageRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @PostMapping("/{messageId}/feedback")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @PathVariable Long messageId, @RequestBody FeedbackRequest request) {
        User user = currentUserService.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        if (!message.getConversation().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this message");
        }

        Feedback.Rating rating;
        try {
            rating = Feedback.Rating.valueOf(request.rating());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be UP or DOWN");
        }

        //update in place if this message already has feedback, so a user can change their mind
        //about a rating rather than accumulating duplicate rows the unique constraint would reject
        Feedback feedback = feedbackRepository.findByMessageId(messageId)
                .orElseGet(() -> {
                    Feedback newFeedback = new Feedback();
                    newFeedback.setMessage(message);
                    return newFeedback;
                });
        feedback.setRating(rating);
        feedback.setComment(request.comment());
        feedbackRepository.save(feedback);

        return ResponseEntity.ok(new FeedbackResponse(messageId, feedback.getRating().name(), feedback.getComment()));
    }
}
