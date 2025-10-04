package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
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
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OccasionStateMachineServiceImpl implements OccasionStateMachineService {

    private final OccasionRepository occasionRepository;
    private final OccasionService occasionService;
    private final StateMachineFactory<OccasionState, OccasionEvent> occasionStateMachine;

    @Override
    public void sendEvent(Long occasionId, OccasionEvent event) {
        OccasionEntity occasion = findOccasionById(occasionId);

        if (!occasionService.isValidTransition(occasion.getState(), event)) {
            throw new IllegalStateException(
                    String.format("Невалидный переход из %s в %s", occasion.getState(), event));
        }
        if (occasionService.canTransition(occasion, event)) {
            StateMachine<OccasionState, OccasionEvent> sm = occasionStateMachine.getStateMachine(occasion.getId().toString());
            
            // Создаем сообщение
            Message<OccasionEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader("occasion", occasion)
                    .build();

            // Инициализируем состояние (синхронно)
            sm.stop();
            sm.getStateMachineAccessor()
                    .doWithAllRegions(access -> access.resetStateMachine(
                            new DefaultStateMachineContext<>(occasion.getState(), null, null, null)
                    ));
            sm.start();

            // Используем реактивный API для отправки события
            sm.sendEvent(Mono.just(message))
                    .doFinally(signalType -> sm.stop())
                    .blockLast(); // Блокируем до завершения всех операций
        }
    }

    private OccasionEntity findOccasionById(Long occasionId) {
        // TODO: Implement entity retrieval logic
        return occasionRepository.findById(occasionId)
                .orElseThrow(() -> new EntityNotFoundException("Occasion not found"));
    }
}
