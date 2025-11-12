package org.bn.sensation.core.activity.service;

import java.util.Optional;
import java.util.Set;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.activity.statemachine.ActivityEvent;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.occasion.statemachine.OccasionState;
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
    public String canTransition(ActivityEntity activity, ActivityEvent event) {
        log.debug("Проверка возможности перехода активности: id={}, событие={}, текущее состояние={}",
                activity.getId(), event, activity.getState());
        return switch (event) {
            case PLAN -> activity.getMilestones().stream()
                    .anyMatch(milestone -> Set.of(
                            MilestoneState.IN_PROGRESS, MilestoneState.SUMMARIZING, MilestoneState.COMPLETED
                    ).contains(milestone.getState()))
                    ? "Нельзя перевести активность в запланированную, т.к. ее этап находится в неподходящем состоянии"
                    : null;
            case CLOSE_REGISTRATION, START -> activity.getOccasion().getState() != OccasionState.IN_PROGRESS
                    ? "Нельзя %s активность, т.к. мероприятие находится в состоянии %s"
                    .formatted(event == ActivityEvent.CLOSE_REGISTRATION ? "закрыть регистрацию" : "начать",
                            activity.getOccasion().getState())
                    : null;
            case SUM_UP -> activity.getMilestones().stream()
                    .anyMatch(ms -> ms.getState() != MilestoneState.COMPLETED && ms.getState() != MilestoneState.SUMMARIZING)
                    ? "Нельзя завершить активность, т.к. есть незавершенные этапы"
                    : null;
            case COMPLETE -> activity.getMilestones().stream()
                    .anyMatch(ms -> ms.getState() != MilestoneState.COMPLETED)
                    ? "Нельзя завершить активность, т.к. есть незавершенные этапы"
                    : null;
        };
    }

    @Override
    public Optional<ActivityState> getNextState(ActivityState currentState, ActivityEvent event) {
        ActivityState nextState = switch (currentState) {
            case PLANNED -> switch (event) {
                case PLAN -> currentState;
                case CLOSE_REGISTRATION -> ActivityState.REGISTRATION_CLOSED;
                default -> null;
            };
            case REGISTRATION_CLOSED -> switch (event) {
                case CLOSE_REGISTRATION -> currentState;
                case PLAN -> ActivityState.PLANNED;
                case START -> ActivityState.IN_PROGRESS;
                default -> null;
            };
            case IN_PROGRESS -> switch (event) {
                case START -> currentState;
                case SUM_UP -> ActivityState.SUMMARIZING;
                default -> null;
            };
            case SUMMARIZING -> switch (event) {
                case SUM_UP -> currentState;
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
