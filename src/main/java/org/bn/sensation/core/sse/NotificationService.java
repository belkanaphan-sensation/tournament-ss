package org.bn.sensation.core.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bn.sensation.security.CurrentUser;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
//@EnableScheduling
public class NotificationService {

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();
    private final CurrentUser currentUser;

    public SseEmitter createEmitter() {
        Long userId = currentUser.getSecurityUser().getId();
        // 0L = без таймаута, соединение живет, пока клиент не отвалится или сервер не закроет
        SseEmitter emitter = new SseEmitter(0L);
        log.debug("Created SSE emitter for user={}", userId);

        emittersByUser.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE completed for user={}", userId);
            removeEmitter(userId, List.of(emitter));
        });

        emitter.onTimeout(() -> {
            log.debug("SSE timeout for user={}", userId);
            removeEmitter(userId, List.of(emitter));
        });

        emitter.onError(ex -> {
            log.warn("SSE error for user={}", userId, ex);
            removeEmitter(userId, List.of(emitter));
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to SSE stream"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event for user={}", userId, e);
            emitter.completeWithError(e);
            removeEmitter(userId, List.of(emitter));
        }

        return emitter;
    }

    public void sendNotificationToAll(String message) {
        log.debug("Sending notification to all users: {}", message);
        emittersByUser.forEach((userId, userEmitters) -> sendToUserInternal(userId, message, userEmitters));
    }

    @Async
    public void sendNotificationToUser(Long userId, String message) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emittersByUser.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("No active SSE connections for user={}, message not sent", userId);
            return;
        }
        sendToUserInternal(userId, message, userEmitters);
    }

    public void sendNotificationToCurrentUser(String message) {
        Long userId = currentUser.getSecurityUser().getId();
        sendNotificationToUser(userId, message);
    }

    private void sendToUserInternal(Long userId, String message, List<SseEmitter> userEmitters) {
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(message));
            } catch (IOException e) {
                log.debug("Removing dead emitter for user={}", userId, e);
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            removeEmitter(userId, deadEmitters);
        }
    }

    private void removeEmitter(Long userId, List<SseEmitter> deadEmitters) {
        CopyOnWriteArrayList<SseEmitter> userEmitters = emittersByUser.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.removeAll(deadEmitters);
        if (userEmitters.isEmpty()) {
            emittersByUser.remove(userId);
        }
    }

//    // Отправка тестового уведомления каждые 10 секунд
//    @Scheduled(fixedRate = 10000) // 10000 ms = 10 секунд
//    public void sendTestNotification() {
//        log.debug(("Sending test notification"));
//        String message = "Test notification at " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
//        sendNotificationToAll(message);
//    }
}