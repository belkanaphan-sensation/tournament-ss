package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.listener.RoundStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
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
public class RoundStateMachineServiceImpl implements RoundStateMachineService {

    private final RoundRepository roundRepository;
    private final RoundService roundService;
    private final StateMachineFactory<RoundState, RoundEvent> roundStateMachine;

    @Override
    public void sendEvent(Long roundId, RoundEvent event) {
        // Устанавливаем ID раунда для логирования
        RoundStateMachineListener.setRoundId(roundId);

        try {
            RoundEntity round = roundRepository.findByIdWithUserAssignments(roundId)
                    .orElseThrow(() -> new EntityNotFoundException("Round not found"));

            log.info("🎯 [ROUND_EVENT_START] Round ID: {} | Event: {} | Current State: {}",
                roundId, event, round.getState());

            // Проверяем, изменится ли состояние
            RoundState nextState = roundService.getNextState(round.getState(), event);
            if (nextState == round.getState()) {
                log.info("ℹ️ [ROUND_EVENT_NO_CHANGE] Round ID: {} | Event: {} | State remains: {}",
                    roundId, event, round.getState());
                return; // Состояние не меняется, просто выходим
            }

            if (!roundService.isValidTransition(round.getState(), event)) {
                log.warn("❌ [ROUND_EVENT_REJECTED] Round ID: {} | Invalid transition from {} to {}",
                    roundId, round.getState(), event);
                throw new IllegalStateException(
                        String.format("Невалидный переход из %s в %s", round.getState(), event));
            }

            if (roundService.canTransition(round, event)) {
                StateMachine<RoundState, RoundEvent> sm = roundStateMachine.getStateMachine(round.getId().toString());

                // Регистрируем связь между State Machine и Round ID
                RoundStateMachineListener.registerStateMachine(sm.getId(), roundId);

                // Создаем сообщение
                Message<RoundEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("round", round)
                        .build();

                log.debug("📤 [ROUND_MESSAGE_CREATED] Round ID: {} | Event: {} | Headers: {}",
                    roundId, event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(round.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [ROUND_SM_INITIALIZED] Round ID: {} | State: {}",
                    roundId, round.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [ROUND_EVENT_COMPLETED] Round ID: {} | Signal: {}",
                                roundId, signalType);
                            sm.stop();
                            RoundStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций

                log.info("✅ [ROUND_EVENT_SUCCESS] Round ID: {} | Event: {} | Final State: {}",
                    roundId, event, round.getState());
            } else {
                log.warn("🚫 [ROUND_EVENT_BLOCKED] Round ID: {} | Event: {} | Reason: Business logic validation failed",
                    roundId, event);
            }
        } finally {
            // Очищаем контекст
            RoundStateMachineListener.clearRoundId();
        }
    }
}
