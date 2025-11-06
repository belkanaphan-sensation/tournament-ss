package org.bn.sensation.core.activity.service;

import java.util.Optional;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

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
        //TODO
        switch (event) {
            case DRAFT -> {
                log.debug("Проверка возможности перевода активности в черновик={}, состояние мероприятия={}",
                        activity.getId(), activity.getOccasion().getState());
                Preconditions.checkState(activity.getOccasion().getState() != OccasionState.COMPLETED,
                        "Нельзя перевести активность в черновик, т.к. мероприятие находится в состоянии %s", activity.getOccasion().getState());
                //TODO Продумать в каких состояниях должны быть этапы активности
                log.debug("Проверка возможности перевода активности в черновик завершена");
            }
            case PLAN -> {
                log.debug("Проверка возможности запланировать активность={}, состояние мероприятия={}",
                        activity.getId(), activity.getOccasion().getState());
                log.debug("Проверка возможности запланировать активность завершена");
            }
            case CLOSE_REGISTRATION -> {
                log.debug("Проверка возможности закрыть регистрацию на активность={}, состояние мероприятия={}",
                        activity.getId(), activity.getOccasion().getState());
                log.debug("Проверка возможности закрыть регистрацию на активность завершена");
            }
            case START -> {
                log.debug("Проверка возможности старта активность={}, состояние мероприятия={}",
                        activity.getId(), activity.getOccasion().getState());
                Preconditions.checkState(activity.getOccasion().getState() == OccasionState.IN_PROGRESS,
                        "Нельзя начать активность, т.к. мероприятие находится в состоянии %s", activity.getOccasion().getState());
                log.debug("Старт активности разрешен");
            }
            case SUM_UP -> {
                log.debug("Проверка возможности подведения итогов активности={}", activity.getId());
                Preconditions.checkState(activity.getMilestones().stream()
                                .allMatch(ms -> ms.getState() == MilestoneState.COMPLETED || ms.getState() == MilestoneState.SUMMARIZING),
                        "Нельзя завершить активность, т.к. есть незавершенные этапы");
            }
            case COMPLETE -> {
                log.debug("Проверка возможности завершить активность={}", activity.getId());
                Preconditions.checkState(activity.getMilestones().stream()
                                .allMatch(ms -> ms.getState() == MilestoneState.COMPLETED),
                        "Нельзя завершить активность, т.к. есть незавершенные этапы");
                log.debug("Проверка возможности завершить активность завершена");
            }
        }
        return true;
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
