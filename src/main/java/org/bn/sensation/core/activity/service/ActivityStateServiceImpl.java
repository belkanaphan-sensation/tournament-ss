package org.bn.sensation.core.activity.service;

import java.util.Optional;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityStateServiceImpl implements BaseStateService<ActivityEntity, ActivityState, ActivityEvent> {

    private final ActivityRepository activityRepository;

    @Override
    public void saveTransition(ActivityEntity activity, ActivityState state) {
        log.debug("Сохранение перехода состояния активности: id={}, новое состояние={}", activity.getId(), state);
        activity.setState(state);
        activityRepository.save(activity);
        log.debug("Переход состояния активности сохранен: id={}, состояние={}", activity.getId(), state);
    }

    @Override
    public boolean canTransition(ActivityEntity activity, ActivityEvent event) {
        log.debug("Проверка возможности перехода активности: id={}, событие={}, текущее состояние={}",
                activity.getId(), event, activity.getState());
        // TODO: Implement business logic for activity transitions
        boolean canTransition = true;
        log.debug("Результат проверки перехода активности: id={}, может перейти={}", activity.getId(), canTransition);
        return canTransition;
    }

    @Override
    public Optional<ActivityState> getNextState(ActivityState currentState, ActivityEvent event) {
        ActivityState nextState = switch (currentState) {
            case DRAFT -> switch (event) {
                case DRAFT -> currentState;
                case PLAN -> ActivityState.PLANNED;
                default -> null;
            };
            case PLANNED -> switch (event) {
                case DRAFT -> ActivityState.DRAFT;
                case PLAN -> currentState;
                case CLOSE_REGISTRATION -> ActivityState.REGISTRATION_CLOSED;
                default -> null;
            };
            case REGISTRATION_CLOSED -> switch (event) {
                case CLOSE_REGISTRATION -> currentState;
                case START -> ActivityState.IN_PROGRESS;
                default -> null;
            };
            case IN_PROGRESS -> switch (event) {
                case START -> currentState;
                case COMPLETE -> ActivityState.COMPLETED;
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
