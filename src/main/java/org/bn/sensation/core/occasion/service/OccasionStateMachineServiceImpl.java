package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.occasion.statemachine.OccasionEvent;
import org.bn.sensation.core.occasion.statemachine.OccasionStateMachineListener;
import org.bn.sensation.core.occasion.statemachine.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
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
public class OccasionStateMachineServiceImpl implements OccasionStateMachineService {

    private final BaseStateService<OccasionEntity, OccasionState, OccasionEvent> occasionStateService;
    private final StateMachineFactory<OccasionState, OccasionEvent> occasionStateMachine;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendEvent(OccasionEntity occasion, OccasionEvent event) {
        // Устанавливаем ID мероприятия для логирования
        OccasionStateMachineListener.setOccasionId(occasion.getId());
        try {
            log.info("🎯 [OCCASION_EVENT_START] Occasion ID: {} | Event: {} | Current State: {}",
                    occasion.getId(), event, occasion.getState());

            if (occasionStateService.getNextState(occasion.getState(), event).isEmpty()) {
                log.warn("❌ [OCCASION_EVENT_REJECTED] Occasion ID: {} | Invalid transition from {} to {}",
                        occasion.getId(), occasion.getState(), event);
                throw new IllegalStateException(
                        String.format("Невалидный переход из %s в %s", occasion.getState(), event));
            }

            String validationError = occasionStateService.canTransition(occasion, event);
            if (validationError != null) {
                log.warn("🚫 [OCCASION_EVENT_BLOCKED] Occasion ID: {} | Event: {} | Reason: {}",
                        occasion.getId(), event, validationError);
                throw new IllegalStateException(validationError);
            }

            StateMachine<OccasionState, OccasionEvent> sm = occasionStateMachine.getStateMachine(occasion.getId().toString());

                // Регистрируем связь между State Machine и Occasion ID
                OccasionStateMachineListener.registerStateMachine(sm.getId(), occasion.getId());

                // Создаем сообщение
                Message<OccasionEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("occasion", occasion)
                        .setHeader("occasionId", occasion.getId())
                        .build();

                log.debug("📤 [OCCASION_MESSAGE_CREATED] Occasion ID: {} | Event: {} | Headers: {}",
                        occasion.getId(), event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(occasion.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [OCCASION_SM_INITIALIZED] Occasion ID: {} | State: {}",
                        occasion.getId(), occasion.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [OCCASION_EVENT_COMPLETED] Occasion ID: {} | Signal: {}",
                                    occasion.getId(), signalType);
                            sm.stop();
                            OccasionStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций

            log.info("✅ [OCCASION_EVENT_SUCCESS] Occasion ID: {} | Event: {} | Final State: {}",
                    occasion.getId(), event, occasion.getState());
        } finally {
            // Очищаем контекст
            OccasionStateMachineListener.clearOccasionId();
        }
    }
}
