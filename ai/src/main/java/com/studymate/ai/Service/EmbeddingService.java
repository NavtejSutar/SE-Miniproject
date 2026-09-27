package com.studymate.ai.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final VectorStore vectorStore;

    public EmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Generate embeddings for text chunks and store them in PgVectorStore
     * with metadata (userId, documentId, fileName, pageNumber, chunkIndex).
     */
    public void embedAndStore(
            List<TextChunkingService.TextChunk> chunks,
            Long userId
    ) {
        if (chunks.isEmpty()) {
            log.warn("No chunks to embed");
            return;
        }

        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(
                        chunk.content(),
                        Map.of(
                                "userId", userId,
                                "documentId", chunk.documentId(),
                                "fileName", chunk.fileName(),
                                "pageNumber", chunk.pageNumber(),
                                "chunkIndex", chunk.chunkIndex()
                        )
                ))
                .toList();

        vectorStore.add(documents);

        log.info("Stored {} embeddings for document {}",
                documents.size(), chunks.getFirst().documentId());
    }

    /**
     * Delete all vector embeddings associated with a specific document.
     */
    public void deleteByDocumentId(Long documentId) {
        vectorStore.delete("documentId == " + documentId);
        log.info("Deleted embeddings for document {}", documentId);
    }
}
