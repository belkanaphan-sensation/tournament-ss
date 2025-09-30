package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
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
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MilestoneStateMachineServiceImpl implements MilestoneStateMachineService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneService milestoneService;
    private final StateMachineFactory<MilestoneState, MilestoneEvent> milestoneStateMachine;

    @Override
    public void sendEvent(Long milestoneId, MilestoneEvent event) {
        MilestoneEntity milestone = findMilestoneById(milestoneId);

        if (!milestoneService.isValidTransition(milestone.getState(), event)) {
            throw new IllegalStateException(
                    String.format("Невалидный переход из %s в %s", milestone.getState(), event));
        }
        if (milestoneService.canTransition(milestone, event)) {
            StateMachine<MilestoneState, MilestoneEvent> sm = milestoneStateMachine.getStateMachine(milestone.getId().toString());
            
            // Создаем сообщение
            Message<MilestoneEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader("milestone", milestone)
                    .build();

            // Инициализируем состояние (синхронно)
            sm.stop();
            sm.getStateMachineAccessor()
                    .doWithAllRegions(access -> access.resetStateMachine(
                            new DefaultStateMachineContext<>(milestone.getState(), null, null, null)
                    ));
            sm.start();

            // Используем реактивный API для отправки события
            sm.sendEvent(Mono.just(message))
                    .doFinally(signalType -> sm.stop())
                    .blockLast(); // Блокируем до завершения всех операций
        }
    }

    private MilestoneEntity findMilestoneById(Long milestoneId) {
        // TODO: Implement entity retrieval logic
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found"));
    }
}
