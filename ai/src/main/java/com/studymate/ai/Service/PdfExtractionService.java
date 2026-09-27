package com.studymate.ai.Service;

import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.DocumentPage;
import com.studymate.ai.Repo.PageRepo;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractionService {

    private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);

    private final PageRepo pageRepo;
    private final OcrService ocrService;

    public PdfExtractionService(PageRepo pageRepo, OcrService ocrService) {
        this.pageRepo = pageRepo;
        this.ocrService = ocrService;
    }

    /**
     * Extract text page-by-page from a PDF and save as DocumentPage entities.
     * If a page has no digital text (e.g. scanned), falls back to OCR if available.
     *
     * @return list of created DocumentPage entities
     */
    public List<DocumentPage> extractAndSavePages(Document document) {
        Path pdfPath = Paths.get(document.getFilePath());
        List<DocumentPage> pages = new ArrayList<>();

        try (PDDocument pdf = Loader.loadPDF(pdfPath.toFile())) {
            int totalPages = pdf.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            PDFRenderer renderer = null;

            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String text = stripper.getText(pdf).trim();

                // Fall back to OCR for scanned pages if text is empty
                if (text.isEmpty() && ocrService.isTesseractAvailable()) {
                    log.info("Page {} of doc {} has no text, attempting OCR fallback", i, document.getDocumentId());
                    if (renderer == null) {
                        renderer = new PDFRenderer(pdf);
                    }
                    try {
                        BufferedImage pageImg = renderer.renderImageWithDPI(i - 1, 150);
                        text = ocrService.extractTextFromRenderedPage(pageImg).trim();
                    } catch (Exception ex) {
                        log.warn("OCR fallback failed on page {}: {}", i, ex.getMessage());
                    }
                }

                // If still empty after OCR or OCR unavailable, skip empty page
                if (text.isEmpty()) {
                    continue;
                }

                DocumentPage page = new DocumentPage();
                page.setPageNumber(i);
                page.setExtractedText(text);
                page.setDocument(document);

                pages.add(page);
            }

            pageRepo.saveAll(pages);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to extract text from PDF: " + document.getFileName(), e
            );
        }

        return pages;
    }
}
