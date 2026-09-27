package com.studymate.ai.Service;

import com.studymate.ai.Dto.DocumentProgressResponse;
import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.DocumentPage;
import com.studymate.ai.Enum.ProcessingStatus;
import com.studymate.ai.Repo.DocumentRepo;
import com.studymate.ai.Repo.PageRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final DocumentRepo documentRepo;
    private final PageRepo pageRepo;
    private final PdfExtractionService pdfExtractionService;
    private final TextChunkingService textChunkingService;
    private final EmbeddingService embeddingService;
    private final OcrService ocrService;
    private final ProgressEmitterService progressEmitterService;

    public DocumentProcessingService(
            DocumentRepo documentRepo,
            PageRepo pageRepo,
            PdfExtractionService pdfExtractionService,
            TextChunkingService textChunkingService,
            EmbeddingService embeddingService,
            OcrService ocrService,
            ProgressEmitterService progressEmitterService
    ) {
        this.documentRepo = documentRepo;
        this.pageRepo = pageRepo;
        this.pdfExtractionService = pdfExtractionService;
        this.textChunkingService = textChunkingService;
        this.embeddingService = embeddingService;
        this.ocrService = ocrService;
        this.progressEmitterService = progressEmitterService;
    }

    /**
     * Full async pipeline with real-time progress updates:
     * 0%: Uploaded
     * 20%: Extracting text
     * 40%: Processing pages
     * 60%: Creating chunks
     * 80%: Generating embeddings
     * 100%: Ready
     */
    @Async
    public void processDocument(Long documentId) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow();

        updateProgress(document, ProcessingStatus.PROCESSING, 20, "Extracting text", null);

        try {
            // Step 1: Extract text
            List<DocumentPage> pages = extractText(document);
            log.info("Document {}: extracted {} pages", documentId, pages.size());

            updateProgress(document, ProcessingStatus.PROCESSING, 40, "Processing pages", null);

            // Step 2: Chunk text
            updateProgress(document, ProcessingStatus.PROCESSING, 60, "Creating chunks", null);
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
                updateProgress(document, ProcessingStatus.PROCESSING, 80, "Generating embeddings", null);
                embeddingService.embedAndStore(allChunks, document.getUser().getId());
                log.info("Document {}: embeddings stored", documentId);
            }

            // Step 4: Done
            updateProgress(document, ProcessingStatus.READY, 100, "Ready", null);
            log.info("Document {} processed successfully", documentId);

        } catch (Exception e) {
            log.error("Failed to process document {}", documentId, e);
            updateProgress(document, ProcessingStatus.FAILED, -1, "Failed: " + e.getMessage(), e.getMessage());
        }
    }

    private void updateProgress(
            Document document,
            ProcessingStatus status,
            int progress,
            String progressMessage,
            String errorMessage
    ) {
        document.setStatus(status);
        document.setProgress(progress);
        document.setProgressMessage(progressMessage);
        document.setErrorMessage(errorMessage);
        documentRepo.save(document);

        DocumentProgressResponse response = new DocumentProgressResponse(
                document.getDocumentId(),
                status,
                progress,
                progressMessage,
                errorMessage
        );

        progressEmitterService.emitProgress(response);
    }

    private List<DocumentPage> extractText(Document document) {
        String contentType = document.getContentType();

        if ("application/pdf".equals(contentType)) {
            return pdfExtractionService.extractAndSavePages(document);
        } else if (contentType != null &&
                (contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            String ocrText = ocrService.extractTextFromImage(Paths.get(document.getFilePath()));
            if (!ocrText.isBlank()) {
                DocumentPage page = new DocumentPage();
                page.setPageNumber(1);
                page.setExtractedText(ocrText);
                page.setDocument(document);
                DocumentPage savedPage = pageRepo.save(page);
                return List.of(savedPage);
            }
            return List.of();
        }

        return List.of();
    }
}
