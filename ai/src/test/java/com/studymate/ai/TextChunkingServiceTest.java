package com.studymate.ai;

import com.studymate.ai.Service.TextChunkingService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkingServiceTest {

    private final TextChunkingService chunkingService = new TextChunkingService();

    @Test
    void testChunkEmptyText() {
        List<TextChunkingService.TextChunk> chunks = chunkingService.chunkText("", 1L, "notes.pdf", 1);
        assertTrue(chunks.isEmpty());

        chunks = chunkingService.chunkText(null, 1L, "notes.pdf", 1);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testChunkSmallText() {
        String shortText = "Operating systems manage hardware resources. They provide common services for programs.";
        List<TextChunkingService.TextChunk> chunks = chunkingService.chunkText(shortText, 10L, "os.pdf", 2);

        assertEquals(1, chunks.size());
        assertEquals(shortText, chunks.getFirst().content());
        assertEquals(10L, chunks.getFirst().documentId());
        assertEquals("os.pdf", chunks.getFirst().fileName());
        assertEquals(2, chunks.getFirst().pageNumber());
        assertEquals(0, chunks.getFirst().chunkIndex());
    }

    @Test
    void testChunkLongTextCreatesMultipleChunks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("Sentence ").append(i).append(" discusses process scheduling and virtual memory algorithms. ");
        }

        List<TextChunkingService.TextChunk> chunks = chunkingService.chunkText(sb.toString(), 42L, "os.pdf", 5);

        assertTrue(chunks.size() > 1, "Should create multiple chunks for long text");
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).chunkIndex());
            assertEquals(42L, chunks.get(i).documentId());
            assertEquals("os.pdf", chunks.get(i).fileName());
            assertEquals(5, chunks.get(i).pageNumber());
            assertFalse(chunks.get(i).content().isBlank());
        }
    }
}
