package com.studymate.ai.Service;

import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.DocumentPage;
import com.studymate.ai.Enum.ProcessingStatus;
import com.studymate.ai.Repo.DocumentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final DocumentRepo documentRepo;
    private final PdfExtractionService pdfExtractionService;
    private final TextChunkingService textChunkingService;
    private final EmbeddingService embeddingService;

    public DocumentProcessingService(
            DocumentRepo documentRepo,
            PdfExtractionService pdfExtractionService,
            TextChunkingService textChunkingService,
            EmbeddingService embeddingService
    ) {
        this.documentRepo = documentRepo;
        this.pdfExtractionService = pdfExtractionService;
        this.textChunkingService = textChunkingService;
        this.embeddingService = embeddingService;
    }

    /**
     * Full async pipeline:
     * 1. PROCESSING
     * 2. Extract text → DocumentPages
     * 3. Chunk text
     * 4. Generate embeddings → PgVectorStore
     * 5. READY (or FAILED)
     */
    @Async
    public void processDocument(Long documentId) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow();

        document.setStatus(ProcessingStatus.PROCESSING);
        documentRepo.save(document);

        try {
            // Step 1: Extract text
            List<DocumentPage> pages = extractText(document);
            log.info("Document {}: extracted {} pages", documentId, pages.size());

            // Step 2: Chunk text
            List<TextChunkingService.TextChunk> allChunks = new ArrayList<>();
            for (DocumentPage page : pages) {
                List<TextChunkingService.TextChunk> pageChunks =
                        textChunkingService.chunkText(
                                page.getExtractedText(),
                                document.getDocumentId(),
                                document.getFileName(),
                                page.getPageNumber()
                        );
                allChunks.addAll(pageChunks);
            }
            log.info("Document {}: created {} chunks", documentId, allChunks.size());

            // Step 3: Generate embeddings and store
            if (!allChunks.isEmpty()) {
                embeddingService.embedAndStore(allChunks, document.getUser().getId());
                log.info("Document {}: embeddings stored", documentId);
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

    private List<DocumentPage> extractText(Document document) {
        String contentType = document.getContentType();

        if ("application/pdf".equals(contentType)) {
            return pdfExtractionService.extractAndSavePages(document);
        } else if (contentType != null &&
                (contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            // TODO: OCR processing for images
            log.warn("OCR not yet implemented. Document {} skipped.", document.getDocumentId());
            return List.of();
        }

        return List.of();
    }
}
