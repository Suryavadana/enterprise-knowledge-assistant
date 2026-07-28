package com.example.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.server.dto.ChatRequest;
import com.example.server.dto.ChatResponse;
import com.example.server.dto.Citation;
import com.example.server.dto.GeminiMessage;
import com.example.server.dto.RetrievedChunk;
import com.example.server.entity.Conversation;
import com.example.server.entity.Message;
import com.example.server.entity.User;
import com.example.server.repository.ConversationRepository;
import com.example.server.repository.MessageRepository;
import com.example.server.service.CurrentUserService;
import com.example.server.service.GeminiService;
import com.example.server.service.RetrievalService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final int RETRIEVAL_TOP_K = 3;

    private final CurrentUserService currentUserService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final GeminiService geminiService;
    private final RetrievalService retrievalService;

    public ChatController(
            CurrentUserService currentUserService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            GeminiService geminiService,
            RetrievalService retrievalService) {
        this.currentUserService = currentUserService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.geminiService = geminiService;
        this.retrievalService = retrievalService;
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

        List<RetrievedChunk> chunks = retrievalService.retrieveRelevantChunks(request.message(), RETRIEVAL_TOP_K);
        if (!chunks.isEmpty()) {
            history = withAugmentedLastTurn(history, chunks, request.message());
        }

        String reply = geminiService.generateReply(history);

        Message assistantMessage = new Message();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole(Message.Role.ASSISTANT);
        assistantMessage.setContent(reply);
        messageRepository.save(assistantMessage);

        List<Citation> citations = toCitations(chunks);
        return ResponseEntity.ok(new ChatResponse(conversation.getId(), reply, citations));
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

    //replaces only the final turn (the question just asked) with a version that prepends
    //retrieved document context - every earlier turn in the history is left as originally stored,
    //since that context was only ever relevant to answering this one question
    private List<GeminiMessage> withAugmentedLastTurn(
            List<GeminiMessage> history, List<RetrievedChunk> chunks, String question) {
        String context = chunks.stream()
                .map(RetrievedChunk::text)
                .collect(Collectors.joining("\n"));
        String augmented = "Context from your documents:\n" + context + "\n\nQuestion: " + question;

        List<GeminiMessage> augmentedHistory = new ArrayList<>(history.subList(0, history.size() - 1));
        GeminiMessage lastTurn = history.get(history.size() - 1);
        augmentedHistory.add(new GeminiMessage(lastTurn.role(), augmented));
        return augmentedHistory;
    }

    private List<Citation> toCitations(List<RetrievedChunk> chunks) {
        return chunks.stream()
                .map(chunk -> {
                    Map<String, Object> metadata = chunk.metadata();
                    String filename = (String) metadata.get("filename");
                    //Jackson picks the narrowest numeric type that fits a JSON number (Integer vs Long),
                    //and Chroma's metadata map is untyped Object values - Number is the common supertype
                    //both can be safely widened from, so a direct cast to Long/int would be a ClassCastException
                    //waiting to happen depending on the value's magnitude
                    Long documentId = ((Number) metadata.get("documentId")).longValue();
                    int chunkIndex = ((Number) metadata.get("chunkIndex")).intValue();
                    return new Citation(filename, documentId, chunkIndex);
                })
                .toList();
    }
}
