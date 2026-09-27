package com.studymate.ai.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);
    private static final int TOP_K = 5;

    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Perform similarity search restricted to the authenticated user's documents.
     * Returns the top K most relevant chunks.
     */
    public List<Document> retrieveRelevantChunks(String query, Long userId) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(TOP_K)
                .filterExpression(b.eq("userId", userId).build())
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        log.info("Retrieved {} chunks for user {} with query: {}",
                results.size(), userId, query.substring(0, Math.min(50, query.length())));

        return results;
    }

    /**
     * Build a context string from retrieved documents for the LLM prompt.
     */
    public String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("=== RELEVANT STUDY MATERIAL ===\n\n");

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String fileName = (String) doc.getMetadata().getOrDefault("fileName", "Unknown");
            Object pageNum = doc.getMetadata().getOrDefault("pageNumber", "?");

            context.append(String.format("[Source %d: %s, Page %s]\n", i + 1, fileName, pageNum));
            context.append(doc.getText());
            context.append("\n\n");
        }

        return context.toString();
    }
}
