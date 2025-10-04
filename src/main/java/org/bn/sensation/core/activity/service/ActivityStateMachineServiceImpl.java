package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.listener.ActivityStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityStateMachineServiceImpl implements ActivityStateMachineService {

    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    private final StateMachineFactory<ActivityState, ActivityEvent> activityStateMachine;

    @Override
    public void sendEvent(Long activityId, ActivityEvent event) {
        // Устанавливаем ID активности для логирования
        ActivityStateMachineListener.setActivityId(activityId);
        
        try {
            ActivityEntity activity = findActivityById(activityId);

            log.info("🎯 [ACTIVITY_EVENT_START] Activity ID: {} | Event: {} | Current State: {}", 
                activityId, event, activity.getState());

            if (!activityService.isValidTransition(activity.getState(), event)) {
                log.warn("❌ [ACTIVITY_EVENT_REJECTED] Activity ID: {} | Invalid transition from {} to {}", 
                    activityId, activity.getState(), event);
                throw new IllegalStateException(
                        String.format("Невалидный переход из %s в %s", activity.getState(), event));
            }
            
            if (activityService.canTransition(activity, event)) {
                StateMachine<ActivityState, ActivityEvent> sm = activityStateMachine.getStateMachine(activity.getId().toString());
                
                // Регистрируем связь между State Machine и Activity ID
                ActivityStateMachineListener.registerStateMachine(sm.getId(), activityId);
                
                // Создаем сообщение
                Message<ActivityEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("activity", activity)
                        .setHeader("activityId", activityId)
                        .build();

                log.debug("📤 [ACTIVITY_MESSAGE_CREATED] Activity ID: {} | Event: {} | Headers: {}", 
                    activityId, event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(activity.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [ACTIVITY_SM_INITIALIZED] Activity ID: {} | State: {}", 
                    activityId, activity.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [ACTIVITY_EVENT_COMPLETED] Activity ID: {} | Signal: {}", 
                                activityId, signalType);
                            sm.stop();
                            ActivityStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций
                        
                log.info("✅ [ACTIVITY_EVENT_SUCCESS] Activity ID: {} | Event: {} | Final State: {}", 
                    activityId, event, activity.getState());
            } else {
                log.warn("🚫 [ACTIVITY_EVENT_BLOCKED] Activity ID: {} | Event: {} | Reason: Business logic validation failed", 
                    activityId, event);
            }
        } finally {
            // Очищаем контекст
            ActivityStateMachineListener.clearActivityId();
        }
    }

    private ActivityEntity findActivityById(Long activityId) {
        // TODO: Implement entity retrieval logic
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found"));
    }
}
