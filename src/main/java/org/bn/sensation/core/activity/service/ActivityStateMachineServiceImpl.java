package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ActivityStateMachineServiceImpl implements ActivityStateMachineService {

    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    private final StateMachineFactory<ActivityState, ActivityEvent> activityStateMachine;

    @Override
    public void sendEvent(Long activityId, ActivityEvent event) {
        ActivityEntity activity = findActivityById(activityId);

        if (!activityService.isValidTransition(activity.getState(), event)) {
            throw new IllegalStateException(
                    String.format("Невалидный переход из %s в %s", activity.getState(), event));
        }
        if (activityService.canTransition(activity, event)) {
            StateMachine<ActivityState, ActivityEvent> sm = activityStateMachine.getStateMachine(activity.getId().toString());

            // Создаем сообщение
            Message<ActivityEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader("activity", activity)
                    .build();

            // Инициализируем состояние (синхронно)
            sm.stop();
            sm.getStateMachineAccessor()
                    .doWithAllRegions(access -> access.resetStateMachine(
                            new DefaultStateMachineContext<>(activity.getState(), null, null, null)
                    ));
            sm.start();

            // Используем реактивный API для отправки события
            sm.sendEvent(Mono.just(message))
                    .doFinally(signalType -> sm.stop())
                    .blockLast(); // Блокируем до завершения всех операций
        }
    }

    private ActivityEntity findActivityById(Long activityId) {
        // TODO: Implement entity retrieval logic
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found"));
    }
}
