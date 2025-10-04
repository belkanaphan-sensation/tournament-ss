package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.listener.OccasionStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
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
public class OccasionStateMachineServiceImpl implements OccasionStateMachineService {

    private final OccasionRepository occasionRepository;
    private final OccasionService occasionService;
    private final StateMachineFactory<OccasionState, OccasionEvent> occasionStateMachine;

    @Override
    public void sendEvent(Long occasionId, OccasionEvent event) {
        // Устанавливаем ID мероприятия для логирования
        OccasionStateMachineListener.setOccasionId(occasionId);

        try {
            OccasionEntity occasion = findOccasionById(occasionId);

            log.info("🎯 [OCCASION_EVENT_START] Occasion ID: {} | Event: {} | Current State: {}",
                occasionId, event, occasion.getState());

            if (!occasionService.isValidTransition(occasion.getState(), event)) {
                log.warn("❌ [OCCASION_EVENT_REJECTED] Occasion ID: {} | Invalid transition from {} to {}",
                    occasionId, occasion.getState(), event);
                throw new IllegalStateException(
                        String.format("Невалидный переход из %s в %s", occasion.getState(), event));
            }

            if (occasionService.canTransition(occasion, event)) {
                StateMachine<OccasionState, OccasionEvent> sm = occasionStateMachine.getStateMachine(occasion.getId().toString());

                // Регистрируем связь между State Machine и Occasion ID
                OccasionStateMachineListener.registerStateMachine(sm.getId(), occasionId);

                // Создаем сообщение
                Message<OccasionEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("occasion", occasion)
                        .setHeader("occasionId", occasionId)
                        .build();

                log.debug("📤 [OCCASION_MESSAGE_CREATED] Occasion ID: {} | Event: {} | Headers: {}",
                    occasionId, event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(occasion.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [OCCASION_SM_INITIALIZED] Occasion ID: {} | State: {}",
                    occasionId, occasion.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [OCCASION_EVENT_COMPLETED] Occasion ID: {} | Signal: {}",
                                occasionId, signalType);
                            sm.stop();
                            OccasionStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций

                log.info("✅ [OCCASION_EVENT_SUCCESS] Occasion ID: {} | Event: {} | Final State: {}",
                    occasionId, event, occasion.getState());
            } else {
                log.warn("🚫 [OCCASION_EVENT_BLOCKED] Occasion ID: {} | Event: {} | Reason: Business logic validation failed",
                    occasionId, event);
            }
        } finally {
            // Очищаем контекст
            OccasionStateMachineListener.clearOccasionId();
        }
    }

    private OccasionEntity findOccasionById(Long occasionId) {
        // TODO: Implement entity retrieval logic
        return occasionRepository.findById(occasionId)
                .orElseThrow(() -> new EntityNotFoundException("Occasion not found"));
    }
}
