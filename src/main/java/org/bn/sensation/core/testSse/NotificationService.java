package org.bn.sensation.core.testSse;

import org.bn.sensation.core.testSse.NotificationController;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@EnableScheduling
public class NotificationService {

    private final NotificationController notificationController;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public NotificationService(NotificationController notificationController) {
        this.notificationController = notificationController;
    }

    @PostConstruct
    public void init() {
        System.out.println("NotificationService initialized");
    }

    // Отправка тестового уведомления каждые 10 секунд
    @Scheduled(fixedRate = 10000) // 10000 ms = 10 секунд
    public void sendTestNotification() {
        String time = LocalDateTime.now().format(formatter);
        String message = "Test notification at " + time;
        notificationController.sendNotification(message);
    }

    // Метод для отправки кастомных уведомлений
    public void sendCustomNotification(String message) {
        notificationController.sendNotification(message);
    }
}