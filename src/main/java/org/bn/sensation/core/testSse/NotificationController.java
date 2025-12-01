package org.bn.sensation.core.testSse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        SseEmitter emitter = new SseEmitter(0L); // 0 = без таймаута

        emitters.add(emitter);

        emitter.onCompletion(() -> {
            System.out.println("SSE completed");
            emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            System.out.println("SSE timeout");
            emitters.remove(emitter);
        });

        emitter.onError((ex) -> {
            System.out.println("SSE error: " + ex.getMessage());
            emitters.remove(emitter);
        });

        // Отправляем приветственное сообщение
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to SSE stream"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    // Метод для отправки уведомлений всем клиентам
    public void sendNotification(String message) {
        System.out.println("Sending notification to " + emitters.size() + " clients: " + message);

        // Создаем список emitters для удаления (тех, которые завершились с ошибкой)
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(message));
            } catch (IOException e) {
                // Если ошибка отправки, помечаем emitter как нерабочий
                deadEmitters.add(emitter);
                System.out.println("Removing dead emitter");
            }
        }

        // Удаляем нерабочие emitters
        emitters.removeAll(deadEmitters);
    }
}