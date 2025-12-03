package org.bn.sensation.core.sse;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
//@EnableScheduling
public class NotificationService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        // 0L = без таймаута, соединение живет, пока клиент не отвалится или сервер не закроет
        SseEmitter emitter = new SseEmitter(0L);
        log.debug("Created SSE emitter {}", emitter);

        emitters.add(emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE completed {}", emitter);
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE timeout {}", emitter);
            emitters.remove(emitter);
        });

        emitter.onError(ex -> {
            log.warn("SSE error {}", emitter, ex);
            emitters.remove(emitter);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to SSE stream"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event {}", emitter, e);
            emitter.completeWithError(e);
            emitters.remove(emitter);
        }

        return emitter;
    }

//    // Отправка тестового уведомления каждые 10 секунд
//    @Scheduled(fixedRate = 10000) // 10000 ms = 10 секунд
//    public void sendTestNotification() {
//        log.debug(("Sending test notification"));
//        String message = "Test notification at " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
//        sendNotification(message);
//    }

    @Async
    public void sendNotificationToAll(String message) {
        log.debug("Sending notification to all users: {}", message);
        sendNotification(message);
    }

    public void sendNotification(String message) {
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(message));
            } catch (IOException e) {
                deadEmitters.add(emitter);
                System.out.println("Removing dead emitter");
            }
        }

        emitters.removeAll(deadEmitters);
    }
}