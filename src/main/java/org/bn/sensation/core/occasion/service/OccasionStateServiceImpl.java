package org.bn.sensation.core.occasion.service;

import java.util.Optional;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OccasionStateServiceImpl implements BaseStateService<OccasionEntity, OccasionState, OccasionEvent> {

    private final OccasionRepository occasionRepository;

    @Override
    public void saveTransition(OccasionEntity occasion, OccasionState state) {
        log.debug("Сохранение перехода состояния мероприятия: id={}, новое состояние={}", occasion.getId(), state);
        occasion.setState(state);
        occasionRepository.save(occasion);
        log.debug("Переход состояния мероприятия сохранен: id={}, состояние={}", occasion.getId(), state);
    }

    @Override
    public boolean canTransition(OccasionEntity occasion, OccasionEvent event) {
        log.debug("Проверка возможности перехода мероприятия: id={}, событие={}, текущее состояние={}",
                occasion.getId(), event, occasion.getState());
        // TODO: Implement business logic for occasion transitions
        boolean canTransition = true;
        log.debug("Результат проверки перехода мероприятия: id={}, может перейти={}", occasion.getId(), canTransition);
        return canTransition;
    }

    @Override
    public Optional<OccasionState> getNextState(OccasionState currentState, OccasionEvent event) {
        OccasionState nextState = switch (currentState) {
            case DRAFT -> switch (event) {
                case PLAN -> OccasionState.PLANNED;
                default -> null;
            };
            case PLANNED -> switch (event) {
                case PLAN -> currentState;
                case START -> OccasionState.IN_PROGRESS;
                default -> null;
            };
            case IN_PROGRESS -> switch (event) {
                case START -> currentState;
                case COMPLETE -> OccasionState.COMPLETED;
                default -> null;
            };
            case COMPLETED -> switch (event) {
                case COMPLETE -> currentState;
                default -> null;
            };
        };
        return Optional.ofNullable(nextState);
    }
}
