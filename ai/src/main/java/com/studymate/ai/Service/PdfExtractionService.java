package com.studymate.ai.Service;

import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.DocumentPage;
import com.studymate.ai.Repo.PageRepo;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractionService {

    private final PageRepo pageRepo;

    public PdfExtractionService(PageRepo pageRepo) {
        this.pageRepo = pageRepo;
    }

    /**
     * Extract text page-by-page from a PDF and save as DocumentPage entities.
     *
     * @return list of created DocumentPage entities
     */
    public List<DocumentPage> extractAndSavePages(Document document) {
        Path pdfPath = Paths.get(document.getFilePath());
        List<DocumentPage> pages = new ArrayList<>();

        try (PDDocument pdf = Loader.loadPDF(pdfPath.toFile())) {
            int totalPages = pdf.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            for (int i = 1; i <= totalPages; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String text = stripper.getText(pdf).trim();

                // Skip pages with no extractable text (scanned/image pages)
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
