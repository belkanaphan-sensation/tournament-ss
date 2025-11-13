package org.bn.sensation.core.milestone.service;

import java.util.Optional;
import java.util.Set;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.judgemilestonestatus.model.JudgeMilestoneStatus;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneStateServiceImpl implements BaseStateService<MilestoneEntity, MilestoneState, MilestoneEvent> {

    private final MilestoneRepository milestoneRepository;
    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;

    @Override
    public void saveTransition(MilestoneEntity milestone, MilestoneState state) {
        milestone.setState(state);
        milestoneRepository.save(milestone);
    }

    @Override
    public String canTransition(MilestoneEntity milestone, MilestoneEvent event) {
        switch (event) {
            case DRAFT, SKIP -> {
                return null;
            }
            case PLAN -> {
                if (!Set.of(ActivityState.PLANNED, ActivityState.REGISTRATION_CLOSED, ActivityState.IN_PROGRESS)
                        .contains(milestone.getActivity().getState())) {
                    return "Нельзя запланировать этап, т.к. активность находится в статусе %s"
                            .formatted(milestone.getActivity().getState());
                }
                if (milestone.getMilestoneRule() == null
                        || milestone.getMilestoneRule().getMilestoneCriteria().isEmpty()) {
                    return "Нельзя запланировать этап т.к. у него не настроены правила или критерии";
                }
                return null;
            }
            case PREPARE_ROUNDS -> {
                if (ActivityState.IN_PROGRESS != milestone.getActivity().getState()) {
                    return "Нельзя подготовить раунды, т.к. активность находится в статусе %s"
                            .formatted(milestone.getActivity().getState());
                }
                return null;
            }
            case START -> {
                if (milestone.getActivity().getState() != ActivityState.IN_PROGRESS) {
                    return "Нельзя стартовать этап, т.к. активность находится в статусе %s"
                            .formatted(milestone.getActivity().getState());
                }
                if (milestone.getRounds().isEmpty() || milestone.getParticipants().isEmpty()) {
                    return "Нельзя стартовать этап т.к. у него не сформированы раунды или отсутствуют участники";
                }
                return null;
            }
            case SUM_UP -> {
                if (milestone.getActivity().getState() != ActivityState.IN_PROGRESS) {
                    return "Нельзя подводить итоги этапа, т.к. активность находится в статусе %s"
                            .formatted(milestone.getActivity().getState());
                }
                boolean anyJudgeNotReady = judgeMilestoneStatusCacheService
                        .getAllJudgesStatusForMilestone(milestone.getId()).stream()
                        .anyMatch(st -> st.getStatus() == JudgeMilestoneStatus.NOT_READY);
                if (anyJudgeNotReady) {
                    return "Результаты этапа готовы для подведения итогов не у всех судей";
                }
                return null;
            }
            case COMPLETE -> {
                boolean allRoundsCompleted = milestone.getRounds()
                        .stream()
                        .allMatch(round -> round.getState() == RoundState.CLOSED);
                if (!allRoundsCompleted) {
                    return "Не все раунды завершены";
                }
                int resultsCount = milestone.getResults().size();
                int participantsCount = milestone.getParticipants().size();
                if (resultsCount != participantsCount) {
                    return "Результаты готовы не для всех участников";
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Optional<MilestoneState> getNextState(MilestoneState currentState, MilestoneEvent event) {
        MilestoneState nextState = switch (currentState) {
            case DRAFT -> switch (event) {
                case DRAFT -> currentState;
                case PLAN -> MilestoneState.PLANNED;
                case SKIP -> MilestoneState.SKIPPED;
                default -> null;
            };
            case PLANNED -> switch (event) {
                case DRAFT -> MilestoneState.DRAFT;
                case PLAN -> currentState;
                case PREPARE_ROUNDS -> MilestoneState.PENDING;
                case SKIP -> MilestoneState.SKIPPED;
                default -> null;
            };
            case PENDING -> switch (event) {
                case PLAN -> MilestoneState.PLANNED;
                case PREPARE_ROUNDS -> currentState;
                case START -> MilestoneState.IN_PROGRESS;
                case SKIP -> MilestoneState.SKIPPED;
                default -> null;
            };
            case IN_PROGRESS -> switch (event) {
                case START -> currentState;
//                case PREPARE_ROUNDS -> MilestoneState.PENDING;
                case SUM_UP -> MilestoneState.SUMMARIZING;
                case SKIP -> MilestoneState.SKIPPED;
                default -> null;
            };
            case SUMMARIZING -> switch (event) {
                case START -> MilestoneState.IN_PROGRESS;
                case SUM_UP -> currentState;
                case COMPLETE -> MilestoneState.COMPLETED;
                case SKIP -> MilestoneState.SKIPPED;
                default -> null;
            };
            case COMPLETED -> switch (event) {
                case COMPLETE -> currentState;
                case SKIP -> MilestoneState.SKIPPED;
                default -> null;
            };
            case SKIPPED -> switch (event) {
                case DRAFT -> MilestoneState.DRAFT;
                case SKIP -> currentState;
                default -> null;
            };
        };
        return Optional.ofNullable(nextState);
    }
}
