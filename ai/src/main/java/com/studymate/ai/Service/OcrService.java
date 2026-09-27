package com.studymate.ai.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    @Value("${tesseract.executable:tesseract}")
    private String tesseractPath;

    @Value("${tesseract.data-path:}")
    private String tessdataPath;

    /**
     * Check if tesseract binary is accessible on the system.
     */
    public boolean isTesseractAvailable() {
        try {
            Process process = new ProcessBuilder(tesseractPath, "--version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.debug("Tesseract is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract text from an image file using Tesseract OCR.
     */
    public String extractTextFromImage(Path imagePath) {
        if (!isTesseractAvailable()) {
            log.warn("Tesseract executable not found at '{}'. OCR skipped.", tesseractPath);
            return "[OCR not available: Tesseract is not installed or configured]";
        }

        List<String> command = new ArrayList<>();
        command.add(tesseractPath);
        command.add(imagePath.toAbsolutePath().toString());
        command.add("stdout");
        command.add("-l");
        command.add("eng");

        if (tessdataPath != null && !tessdataPath.isBlank()) {
            command.add("--tessdata-dir");
            command.add(tessdataPath);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Tesseract exited with code {}: {}", exitCode, output);
                return "";
            }

            return output.toString().trim();
        } catch (Exception e) {
            log.error("Error executing OCR on image {}: {}", imagePath, e.getMessage());
            return "";
        }
    }

    /**
     * Perform OCR on a scanned PDF page rendered as a BufferedImage.
     */
    public String extractTextFromRenderedPage(BufferedImage image) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("ocr_page_", ".png");
            ImageIO.write(image, "PNG", tempFile.toFile());
            return extractTextFromImage(tempFile);
        } catch (IOException e) {
            log.error("Failed to write temporary image for OCR: {}", e.getMessage());
            return "";
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Render each page of a scanned PDF to an image and run OCR on it.
     */
    public List<String> extractTextFromScannedPdf(Path pdfPath) {
        List<String> pageTexts = new ArrayList<>();
        if (!isTesseractAvailable()) {
            log.warn("Tesseract is not available for scanned PDF: {}", pdfPath);
            return pageTexts;
        }

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int i = 0; i < pageCount; i++) {
                // Render at 150 DPI for good OCR balance of speed and clarity
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 150);
                String text = extractTextFromRenderedPage(pageImage);
                pageTexts.add(text);
            }
        } catch (IOException e) {
            log.error("Failed to render scanned PDF for OCR: {}", pdfPath, e);
        }

        return pageTexts;
    }
}
