package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.listener.MilestoneStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
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
public class MilestoneStateMachineServiceImpl implements MilestoneStateMachineService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneService milestoneService;
    private final StateMachineFactory<MilestoneState, MilestoneEvent> milestoneStateMachine;

    @Override
    public void sendEvent(Long milestoneId, MilestoneEvent event) {
        // Устанавливаем ID этапа для логирования
        MilestoneStateMachineListener.setMilestoneId(milestoneId);
        
        try {
            MilestoneEntity milestone = findMilestoneById(milestoneId);

            log.info("🎯 [MILESTONE_EVENT_START] Milestone ID: {} | Event: {} | Current State: {}", 
                milestoneId, event, milestone.getState());

            if (!milestoneService.isValidTransition(milestone.getState(), event)) {
                log.warn("❌ [MILESTONE_EVENT_REJECTED] Milestone ID: {} | Invalid transition from {} to {}", 
                    milestoneId, milestone.getState(), event);
                throw new IllegalStateException(
                        String.format("Невалидный переход из %s в %s", milestone.getState(), event));
            }
            
            if (milestoneService.canTransition(milestone, event)) {
                StateMachine<MilestoneState, MilestoneEvent> sm = milestoneStateMachine.getStateMachine(milestone.getId().toString());
                
                // Регистрируем связь между State Machine и Milestone ID
                MilestoneStateMachineListener.registerStateMachine(sm.getId(), milestoneId);
                
                // Создаем сообщение
                Message<MilestoneEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader("milestone", milestone)
                        .setHeader("milestoneId", milestoneId)
                        .build();

                log.debug("📤 [MILESTONE_MESSAGE_CREATED] Milestone ID: {} | Event: {} | Headers: {}", 
                    milestoneId, event, message.getHeaders());

                // Инициализируем состояние (синхронно)
                sm.stop();
                sm.getStateMachineAccessor()
                        .doWithAllRegions(access -> access.resetStateMachine(
                                new DefaultStateMachineContext<>(milestone.getState(), null, null, null)
                        ));
                sm.start();

                log.debug("🔄 [MILESTONE_SM_INITIALIZED] Milestone ID: {} | State: {}", 
                    milestoneId, milestone.getState());

                // Используем реактивный API для отправки события
                sm.sendEvent(Mono.just(message))
                        .doFinally(signalType -> {
                            log.debug("🏁 [MILESTONE_EVENT_COMPLETED] Milestone ID: {} | Signal: {}", 
                                milestoneId, signalType);
                            sm.stop();
                            MilestoneStateMachineListener.unregisterStateMachine(sm.getId());
                        })
                        .blockLast(); // Блокируем до завершения всех операций
                        
                log.info("✅ [MILESTONE_EVENT_SUCCESS] Milestone ID: {} | Event: {} | Final State: {}", 
                    milestoneId, event, milestone.getState());
            } else {
                log.warn("🚫 [MILESTONE_EVENT_BLOCKED] Milestone ID: {} | Event: {} | Reason: Business logic validation failed", 
                    milestoneId, event);
            }
        } finally {
            // Очищаем контекст
            MilestoneStateMachineListener.clearMilestoneId();
        }
    }

    private MilestoneEntity findMilestoneById(Long milestoneId) {
        // TODO: Implement entity retrieval logic
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found"));
    }
}
