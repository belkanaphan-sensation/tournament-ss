package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.activity.statemachine.ActivityEvent;
import org.bn.sensation.core.activity.statemachine.ActivityStateMachineListener;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityStateMachineServiceImpl implements ActivityStateMachineService {

    private final BaseStateService<ActivityEntity, ActivityState, ActivityEvent> activityStateService;
    private final StateMachineFactory<ActivityState, ActivityEvent> activityStateMachine;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendEvent(ActivityEntity activity, ActivityEvent event) {
        // Устанавливаем ID активности для логирования
        ActivityStateMachineListener.setActivityId(activity.getId());
        try {
            log.info("🎯 [ACTIVITY_EVENT_START] Activity ID: {} | Event: {} | Current State: {}",
                    activity.getId(), event, activity.getState());

            if (activityStateService.getNextState(activity.getState(), event).isEmpty()) {
                log.warn("❌ [ACTIVITY_EVENT_REJECTED] Activity ID: {} | Invalid transition from {} to {}",
                        activity.getId(), activity.getState(), event);
                throw new IllegalStateException(
                        String.format("Невалидный переход из %s в %s", activity.getState(), event));
            }

            String validationError = activityStateService.canTransition(activity, event);
            if (validationError != null) {
                log.warn("🚫 [ACTIVITY_EVENT_BLOCKED] Activity ID: {} | Event: {} | Reason: {}", activity.getId(), event, validationError);
                throw new IllegalStateException(validationError);
            }

            StateMachine<ActivityState, ActivityEvent> sm = activityStateMachine.getStateMachine(activity.getId().toString());

                // Регистрируем связь между State Machine и Activity ID
                ActivityStateMachineListener.registerStateMachine(sm.getId(), activity.getId());

                // Создаем сообщение
                Message<ActivityEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("activity", activity)
                        .setHeader("activityId", activity.getId())
                        .build();

                log.debug("📤 [ACTIVITY_MESSAGE_CREATED] Activity ID: {} | Event: {} | Headers: {}",
                        activity.getId(), event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(activity.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [ACTIVITY_SM_INITIALIZED] Activity ID: {} | State: {}",
                        activity.getId(), activity.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [ACTIVITY_EVENT_COMPLETED] Activity ID: {} | Signal: {}",
                                    activity.getId(), signalType);
                            sm.stop();
                            ActivityStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций

            log.info("✅ [ACTIVITY_EVENT_SUCCESS] Activity ID: {} | Event: {} | Final State: {}",
                    activity.getId(), event, activity.getState());
        } finally {
            // Очищаем контекст
            ActivityStateMachineListener.clearActivityId();
        }
    }
}
