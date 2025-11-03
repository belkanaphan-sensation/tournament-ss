package org.bn.sensation.core.milestone.service;

import java.util.Optional;
import java.util.Set;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judgemilestonestatus.model.JudgeMilestoneStatus;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

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
    public boolean canTransition(MilestoneEntity milestone, MilestoneEvent event) {
        switch (event) {
            case DRAFT -> {
            }
            case PLAN -> {
                Preconditions.checkState(Set.of(ActivityState.PLANNED, ActivityState.REGISTRATION_CLOSED, ActivityState.IN_PROGRESS)
                                .contains(milestone.getActivity().getState()),
                        "Нельзя запланировать этап, т.к. активность находится в статусе %s", milestone.getActivity().getState());
                Preconditions.checkArgument(milestone.getMilestoneRule() != null
                                && !milestone.getMilestoneRule().getMilestoneCriteria().isEmpty(),
                        "Нельзя запланировать этап т.к. у него не настроены правила или критерии");
                Set<RoundState> roundStates = Set.of(RoundState.IN_PROGRESS, RoundState.READY, RoundState.COMPLETED);
                Preconditions.checkState(milestone.getRounds().stream().noneMatch(round -> roundStates.contains(round.getState())),
                        "Нельзя планировать этап т.к. его раунды уже в процессе или завершены");
            }
            case START -> {
                Preconditions.checkState(milestone.getActivity().getState() == ActivityState.IN_PROGRESS,
                        "Нельзя стартовать этап, т.к. активность находится в статусе %s", milestone.getActivity().getState());
                Preconditions.checkArgument(!milestone.getRounds().isEmpty() && !milestone.getParticipants().isEmpty(),
                        "Нельзя стартовать этап т.к. у него не сформированы раунды или отсутствуют участники");
            }
            case SUM_UP -> {
                Preconditions.checkState(milestone.getActivity().getState() == ActivityState.IN_PROGRESS,
                        "Нельзя подводить итоги этапа, т.к. активность находится в статусе %s", milestone.getActivity().getState());
                Preconditions.checkArgument(judgeMilestoneStatusCacheService
                                .getAllJudgesStatusForMilestone(milestone.getId()).stream()
                                .noneMatch(st -> st.getStatus() == JudgeMilestoneStatus.NOT_READY),
                        "Результаты этапа готовы для подведения итогов не у всех судей");
                Preconditions.checkState(milestone.getRounds().stream()
                                .allMatch(r -> r.getState() == RoundState.READY || r.getState() == RoundState.COMPLETED),
                        "Все раунды этапа должны быть завершены");
            }
            case COMPLETE -> {
                boolean allRoundsCompleted = milestone.getRounds()
                        .stream()
                        .allMatch(round -> round.getState() == RoundState.COMPLETED);
                Preconditions.checkState(allRoundsCompleted, "Не все раунды завершены");
                int resultsCount = milestone.getResults().size();
                int participantsCount = milestone.getParticipants().size();
                Preconditions.checkState(resultsCount == participantsCount,
                        "Результаты готовы не для всех участников");
            }
        }
        return true;
    }

    @Override
    public Optional<MilestoneState> getNextState(MilestoneState currentState, MilestoneEvent event) {
        MilestoneState nextState = switch (currentState) {
            case DRAFT -> switch (event) {
                case DRAFT -> currentState;
                case PLAN -> MilestoneState.PLANNED;
                default -> null;
            };
            case PLANNED -> switch (event) {
                case DRAFT -> MilestoneState.DRAFT;
                case PLAN -> currentState;
                case PREPARE_ROUNDS -> MilestoneState.PENDING;
                default -> null;
            };
            case PENDING -> switch (event) {
                case PLAN -> MilestoneState.PLANNED;
                case PREPARE_ROUNDS -> currentState;
                case START -> MilestoneState.IN_PROGRESS;
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
        };
        return Optional.ofNullable(nextState);
    }
}
