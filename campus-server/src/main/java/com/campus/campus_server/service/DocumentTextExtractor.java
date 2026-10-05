package com.campus.campus_server.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.InputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DocumentTextExtractor {

    public String extract(Path filePath, String fileType) throws IOException {
        if ("txt".equalsIgnoreCase(fileType)) {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        }

        if ("pdf".equalsIgnoreCase(fileType)) {
            try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
                return new PDFTextStripper().getText(document);
            }
        }

        if ("docx".equalsIgnoreCase(fileType)) {
            try (InputStream input = Files.newInputStream(filePath);
                 XWPFDocument document = new XWPFDocument(input);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        }

        throw new IllegalArgumentException("目前只支持解析 TXT、PDF、DOCX 文件");
    }
}