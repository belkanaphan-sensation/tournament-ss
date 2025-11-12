package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.milestone.statemachine.MilestoneStateMachineListener;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
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
public class MilestoneStateMachineServiceImpl implements MilestoneStateMachineService {

    private final BaseStateService<MilestoneEntity, MilestoneState, MilestoneEvent> milestoneStateService;
    private final StateMachineFactory<MilestoneState, MilestoneEvent> milestoneStateMachine;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void sendEvent(MilestoneEntity milestone, MilestoneEvent event) {
        MilestoneStateMachineListener.setMilestoneId(milestone.getId());
        try {
            log.info("🎯 [MILESTONE_EVENT_START] Milestone ID: {} | Event: {} | Current State: {}",
                    milestone.getId(), event, milestone.getState());

            MilestoneState nextState = milestoneStateService.getNextState(milestone.getState(), event)
                    .orElseThrow(() -> {
                        log.warn("❌ [MILESTONE_EVENT_REJECTED] Milestone ID: {} | Invalid transition from {} to {}",
                                milestone.getId(), milestone.getState(), event);
                        return new IllegalStateException(
                                String.format("Невалидный переход из %s в %s", milestone.getState(), event));
                    });

            if (nextState == milestone.getState()) {
                log.info("ℹ️ [MILESTONE_EVENT_NO_CHANGE] Milestone ID: {} | Event: {} | State remains: {}",
                        milestone.getId(), event, milestone.getState());
                return;
            }

            String validationError = milestoneStateService.canTransition(milestone, event);
            if (validationError != null) {
                log.warn("🚫 [MILESTONE_EVENT_BLOCKED] Milestone ID: {} | Event: {} | Reason: {}",
                        milestone.getId(), event, validationError);
                throw new IllegalStateException(validationError);
            }

            StateMachine<MilestoneState, MilestoneEvent> sm = milestoneStateMachine.getStateMachine(milestone.getId().toString());

                // Регистрируем связь между State Machine и Milestone ID
                MilestoneStateMachineListener.registerStateMachine(sm.getId(), milestone.getId());

                // Создаем сообщение
                Message<MilestoneEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("milestone", milestone)
                        .build();

                log.debug("📤 [MILESTONE_MESSAGE_CREATED] Milestone ID: {} | Event: {} | Headers: {}",
                        milestone.getId(), event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(milestone.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [MILESTONE_SM_INITIALIZED] Milestone ID: {} | State: {}",
                        milestone.getId(), milestone.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [MILESTONE_EVENT_COMPLETED] Milestone ID: {} | Signal: {}",
                                    milestone.getId(), signalType);
                            sm.stop();
                            MilestoneStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций

            log.info("✅ [MILESTONE_EVENT_SUCCESS] Milestone ID: {} | Event: {} | Final State: {}",
                    milestone.getId(), event, milestone.getState());
        } finally {
            // Очищаем контекст
            MilestoneStateMachineListener.clearMilestoneId();
        }
    }

}
