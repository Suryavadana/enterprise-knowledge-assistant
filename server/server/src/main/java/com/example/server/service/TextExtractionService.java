package com.example.server.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.server.entity.Document;

@Service
public class TextExtractionService {

    public String extractText(MultipartFile file, Document.FileType fileType) {
        return switch (fileType) {
            case TXT -> extractTxt(file);
            case PDF -> extractPdf(file);
            case DOCX -> extractDocx(file);
        };
    }

    private String extractTxt(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TextExtractionException("Failed to read text file: " + file.getOriginalFilename(), e);
        }
    }

    private String extractPdf(MultipartFile file) {
        //Loader.loadPDF(...) reads the whole PDF into a PDDocument - an in-memory object graph of pages,
        //fonts, and content streams that holds native resources until closed, hence try-with-resources
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new TextExtractionException("Failed to extract text from PDF: " + file.getOriginalFilename(), e);
        }
    }

    private String extractDocx(MultipartFile file) {
        //two resources here: the XWPFDocument (the parsed .docx, itself a zip of XML parts) and the
        //extractor built on top of it - try-with-resources closes them in reverse declaration order
        //(extractor, then document), which is the order that's actually safe to close them in
        try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            throw new TextExtractionException("Failed to extract text from DOCX: " + file.getOriginalFilename(), e);
        }
    }
}
