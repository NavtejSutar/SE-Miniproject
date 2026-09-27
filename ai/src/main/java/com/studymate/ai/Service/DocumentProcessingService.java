package com.studymate.ai.Service;

import com.studymate.ai.Entities.Document;
import com.studymate.ai.Enum.ProcessingStatus;
import com.studymate.ai.Repo.DocumentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final DocumentRepo documentRepo;
    private final PdfExtractionService pdfExtractionService;

    public DocumentProcessingService(
            DocumentRepo documentRepo,
            PdfExtractionService pdfExtractionService
    ) {
        this.documentRepo = documentRepo;
        this.pdfExtractionService = pdfExtractionService;
    }

    /**
     * Process a document asynchronously:
     * 1. Set status to PROCESSING
     * 2. Extract text (PDF → PDFBox, images → OCR later)
     * 3. Set status to READY on success, FAILED on error
     */
    @Async
    public void processDocument(Long documentId) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow();

        document.setStatus(ProcessingStatus.PROCESSING);
        documentRepo.save(document);

        try {
            String contentType = document.getContentType();

            if ("application/pdf".equals(contentType)) {
                pdfExtractionService.extractAndSavePages(document);
            } else if (contentType != null &&
                    (contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
                // TODO: OCR processing for images (Phase 2 - Tesseract)
                log.warn("OCR not yet implemented for images. Document {} skipped.", documentId);
            }

            document.setStatus(ProcessingStatus.READY);
            document.setErrorMessage(null);
            documentRepo.save(document);

            log.info("Document {} processed successfully", documentId);

        } catch (Exception e) {
            log.error("Failed to process document {}", documentId, e);
            document.setStatus(ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepo.save(document);
        }
    }
}
