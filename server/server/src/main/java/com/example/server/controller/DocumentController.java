package com.example.server.controller;

import java.util.Locale;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.server.dto.DocumentSummary;
import com.example.server.entity.Document;
import com.example.server.entity.User;
import com.example.server.repository.DocumentRepository;
import com.example.server.service.CurrentUserService;
import com.example.server.service.TextExtractionException;
import com.example.server.service.TextExtractionService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final CurrentUserService currentUserService;
    private final DocumentRepository documentRepository;
    private final TextExtractionService textExtractionService;

    public DocumentController(
            CurrentUserService currentUserService,
            DocumentRepository documentRepository,
            TextExtractionService textExtractionService) {
        this.currentUserService = currentUserService;
        this.documentRepository = documentRepository;
        this.textExtractionService = textExtractionService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentSummary>> listDocuments() {
        User user = currentUserService.getCurrentUser();

        List<DocumentSummary> summaries = documentRepository.findByUserOrderByUploadedAtDesc(user).stream()
                .map(document -> new DocumentSummary(
                        document.getId(), document.getFilename(), document.getFileType().name(), document.getUploadedAt()))
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentSummary> upload(@RequestParam("file") MultipartFile file) {
        User user = currentUserService.getCurrentUser();

        String filename = file.getOriginalFilename();
        Document.FileType fileType = resolveFileType(filename);

        String extractedText;
        try {
            extractedText = textExtractionService.extractText(file, fileType);
        } catch (TextExtractionException e) {
            //a parsing failure here means the upload itself was bad (e.g. corrupted/malformed file),
            //so it's a 400 for the client, same as the extension check above - not a 500
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

        Document document = new Document();
        document.setUser(user);
        document.setFilename(filename);
        document.setFileType(fileType);
        document.setExtractedText(extractedText);

        Document saved = documentRepository.save(document);

        return ResponseEntity.ok(new DocumentSummary(
                saved.getId(), saved.getFilename(), saved.getFileType().name(), saved.getUploadedAt()));
    }

    private Document.FileType resolveFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File must have a .pdf, .docx, or .txt extension");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        return switch (extension) {
            case "pdf" -> Document.FileType.PDF;
            case "docx" -> Document.FileType.DOCX;
            case "txt" -> Document.FileType.TXT;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported file extension: ." + extension + " (expected .pdf, .docx, or .txt)");
        };
    }
}
