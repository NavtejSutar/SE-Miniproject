package com.studymate.ai.Service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 200;

    /**
     * Split text into overlapping chunks.
     * Each chunk retains pageNumber and document context via the returned records.
     */
    public List<TextChunk> chunkText(String text, Long documentId, String fileName, int pageNumber) {
        List<TextChunk> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;
        int chunkIndex = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());

            // Try to break at a sentence boundary
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);

                if (breakPoint > start + CHUNK_SIZE / 2) {
                    end = breakPoint + 1;
                }
            }

            String chunkContent = text.substring(start, end).trim();

            if (!chunkContent.isEmpty()) {
                chunks.add(new TextChunk(
                        chunkContent,
                        documentId,
                        fileName,
                        pageNumber,
                        chunkIndex
                ));
                chunkIndex++;
            }

            start = end - CHUNK_OVERLAP;
            if (start >= text.length()) break;
            if (end >= text.length()) break;
        }

        return chunks;
    }

    public record TextChunk(
            String content,
            Long documentId,
            String fileName,
            int pageNumber,
            int chunkIndex
    ) {}
}
