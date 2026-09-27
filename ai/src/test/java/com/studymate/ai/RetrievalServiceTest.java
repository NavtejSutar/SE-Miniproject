package com.studymate.ai;

import com.studymate.ai.Service.RetrievalService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetrievalServiceTest {

    @Test
    void testBuildContextWithEmptyList() {
        VectorStore vectorStore = mock(VectorStore.class);
        RetrievalService retrievalService = new RetrievalService(vectorStore);

        String context = retrievalService.buildContext(List.of());
        assertEquals("", context);
    }

    @Test
    void testBuildContextFormatsMetadataCorrectly() {
        VectorStore vectorStore = mock(VectorStore.class);
        RetrievalService retrievalService = new RetrievalService(vectorStore);

        Document doc1 = new Document("Process synchronization ensures data consistency.", Map.of(
                "fileName", "OS_Chapter3.pdf",
                "pageNumber", 14
        ));
        Document doc2 = new Document("Semaphores can be counting or binary.", Map.of(
                "fileName", "OS_Chapter3.pdf",
                "pageNumber", 18
        ));

        String context = retrievalService.buildContext(List.of(doc1, doc2));

        assertTrue(context.contains("=== RELEVANT STUDY MATERIAL ==="));
        assertTrue(context.contains("[Source 1: OS_Chapter3.pdf, Page 14]"));
        assertTrue(context.contains("Process synchronization ensures data consistency."));
        assertTrue(context.contains("[Source 2: OS_Chapter3.pdf, Page 18]"));
        assertTrue(context.contains("Semaphores can be counting or binary."));
    }
}
