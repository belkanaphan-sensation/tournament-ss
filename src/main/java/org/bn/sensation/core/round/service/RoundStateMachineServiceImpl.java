package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.round.statemachine.RoundEvent;
import org.bn.sensation.core.round.statemachine.RoundStateMachineListener;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.round.entity.RoundEntity;
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
public class RoundStateMachineServiceImpl implements RoundStateMachineService {

    private final BaseStateService<RoundEntity, RoundState, RoundEvent> roundStateService;
    private final StateMachineFactory<RoundState, RoundEvent> roundStateMachine;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendEvent(RoundEntity round, RoundEvent event) {
        // Устанавливаем ID раунда для логирования
        RoundStateMachineListener.setRoundId(round.getId());
        try {
            log.info("🎯 [ROUND_EVENT_START] Round ID: {} | Event: {} | Current State: {}",
                    round.getId(), event, round.getState());

            RoundState nextState = roundStateService.getNextState(round.getState(), event)
                    .orElseThrow(() -> {
                        log.warn("❌ [ROUND_EVENT_REJECTED] Round ID: {} | Invalid transition from {} to {}",
                                round.getId(), round.getState(), event);
                        return new IllegalStateException(
                                String.format("Невалидный переход из %s в %s", round.getState(), event));
                    });

            if (nextState == round.getState()) {
                log.info("ℹ️ [ROUND_EVENT_NO_CHANGE] Round ID: {} | Event: {} | State remains: {}",
                        round.getId(), event, round.getState());
                return; // Состояние не меняется, но переход валиден - просто выходим
            }

            String validationError = roundStateService.canTransition(round, event);
            if (validationError != null) {
                log.warn("🚫 [ROUND_EVENT_BLOCKED] Round ID: {} | Event: {} | Reason: {}",
                        round.getId(), event, validationError);
                throw new IllegalStateException(validationError);
            }

            StateMachine<RoundState, RoundEvent> sm = roundStateMachine.getStateMachine(round.getId().toString());

                // Регистрируем связь между State Machine и Round ID
                RoundStateMachineListener.registerStateMachine(sm.getId(), round.getId());

                // Создаем сообщение
                Message<RoundEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("round", round)
                        .build();

                log.debug("📤 [ROUND_MESSAGE_CREATED] Round ID: {} | Event: {} | Headers: {}",
                        round.getId(), event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(round.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [ROUND_SM_INITIALIZED] Round ID: {} | State: {}",
                        round.getId(), round.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [ROUND_EVENT_COMPLETED] Round ID: {} | Signal: {}",
                                    round.getId(), signalType);
                            sm.stop();
                            RoundStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций

            log.info("✅ [ROUND_EVENT_SUCCESS] Round ID: {} | Event: {} | Final State: {}",
                    round.getId(), event, round.getState());
        } finally {
            // Очищаем контекст
            RoundStateMachineListener.clearRoundId();
        }
    }
}
