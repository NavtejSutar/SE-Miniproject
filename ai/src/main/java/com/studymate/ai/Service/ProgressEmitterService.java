package com.studymate.ai.Service;

import com.studymate.ai.Dto.DocumentProgressResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ProgressEmitterService {

    private static final Logger log = LoggerFactory.getLogger(ProgressEmitterService.class);
    private static final Long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 10 minutes

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long documentId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.computeIfAbsent(documentId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(documentId, emitter));
        emitter.onTimeout(() -> removeEmitter(documentId, emitter));
        emitter.onError(e -> removeEmitter(documentId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to progress stream for document " + documentId));
        } catch (IOException e) {
            removeEmitter(documentId, emitter);
        }

        return emitter;
    }

    public void emitProgress(DocumentProgressResponse progress) {
        List<SseEmitter> documentEmitters = emitters.get(progress.documentId());
        if (documentEmitters == null || documentEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : documentEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("PROGRESS")
                        .data(progress));

                if (progress.progress() != null && (progress.progress() >= 100 || progress.progress() < 0)) {
                    emitter.complete();
                    removeEmitter(progress.documentId(), emitter);
                }
            } catch (Exception e) {
                removeEmitter(progress.documentId(), emitter);
            }
        }
    }

    private void removeEmitter(Long documentId, SseEmitter emitter) {
        List<SseEmitter> documentEmitters = emitters.get(documentId);
        if (documentEmitters != null) {
            documentEmitters.remove(emitter);
            if (documentEmitters.isEmpty()) {
                emitters.remove(documentId);
            }
        }
    }
}
