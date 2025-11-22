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
            case DRAFT -> {
                return null;
            }
            case SKIP -> {
                Optional<MilestoneEntity> previous = milestoneRepository.findByActivityIdAndMilestoneOrder(milestone.getActivity().getId(), milestone.getMilestoneOrder() + 1);
                if (previous.isPresent() && previous.get().getState() != MilestoneState.SKIPPED) {
                    return "Нельзя пропустить этап т.к. у него имеется предыдущий этап %s".formatted(previous.get().getId());
                }
                return null;
            }
            case PLAN -> {
                if (!Set.of(ActivityState.PLANNED, ActivityState.REGISTRATION_CLOSED, ActivityState.IN_PROGRESS)
                        .contains(milestone.getActivity().getState())) {
                    return "Нельзя запланировать этап, т.к. активность находится в состоянии %s"
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
                    return "Нельзя подготовить раунды, т.к. активность находится в состоянии %s"
                            .formatted(milestone.getActivity().getState());
                }
                Optional<MilestoneEntity> previous = milestoneRepository.findByActivityIdAndMilestoneOrder(milestone.getActivity().getId(), milestone.getMilestoneOrder() + 1);
                if (previous.isPresent() && previous.get().getState() != MilestoneState.SKIPPED && previous.get().getState() != MilestoneState.COMPLETED) {
                    return "Нельзя подготовить раунды, т.к. предыдущий этап находится в состоянии %s"
                            .formatted(previous.get().getState());
                }
                if (milestone.getContestants().isEmpty()) {
                    return "Нельзя подготовить раунды, т.к. в этапе отсутствуют конкурсанты";
                }
                return null;
            }
            case START -> {
                if (milestone.getActivity().getState() != ActivityState.IN_PROGRESS) {
                    return "Нельзя стартовать этап, т.к. активность находится в статусе %s"
                            .formatted(milestone.getActivity().getState());
                }
                if (milestone.getRounds().isEmpty()) {
                    return "Нельзя стартовать этап т.к. у него не сформированы раунды";
                }
                return null;
            }
            case SUM_UP -> {
                if (milestone.getActivity().getState() != ActivityState.IN_PROGRESS) {
                    return "Нельзя подводить итоги этапа, т.к. активность находится в состоянии %s"
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
                int contestantCount = milestone.getContestants().size();
                if (resultsCount != contestantCount) {
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
                default -> null;
            };
            case SUMMARIZING -> switch (event) {
                case START -> MilestoneState.IN_PROGRESS;
                case SUM_UP -> currentState;
                case COMPLETE -> MilestoneState.COMPLETED;
                default -> null;
            };
            case COMPLETED -> switch (event) {
                case COMPLETE -> currentState;
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
