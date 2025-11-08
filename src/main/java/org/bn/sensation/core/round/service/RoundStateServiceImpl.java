package org.bn.sensation.core.round.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoundStateServiceImpl implements BaseStateService<RoundEntity, RoundState, RoundEvent> {

    private final RoundRepository roundRepository;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;

    @Override
    @Transactional
    public void saveTransition(RoundEntity round, RoundState state) {
        round.setState(state);
        roundRepository.save(round);
    }

    @Override
    public boolean canTransition(RoundEntity round, RoundEvent event) {
        log.debug("Проверка возможности перехода раунда={} из состояния={} по событию={}",
                round.getId(), round.getState(), event);

        return switch (event) {
            case DRAFT -> {
                log.debug("Проверка возможности перевода раунда в черновик={}, состояние этапа={}",
                        round.getId(), round.getMilestone().getState());
                Preconditions.checkState(Set.of(MilestoneState.SKIPPED, MilestoneState.DRAFT, MilestoneState.PLANNED, MilestoneState.PENDING, MilestoneState.IN_PROGRESS).contains(round.getMilestone().getState()),
                        "Нельзя перевести раунд в черновик, т.к. этап находится в статусе %s", round.getMilestone().getState());
                log.debug("Проверка возможности перевода раунда в черновик завершена");
                yield true;
            }
            case PLAN -> {
                log.debug("Проверка возможности запланировать раунд={}, состояние этапа={}",
                        round.getId(), round.getMilestone().getState());
                Preconditions.checkState(Set.of(MilestoneState.PLANNED, MilestoneState.PENDING, MilestoneState.IN_PROGRESS, MilestoneState.SUMMARIZING).contains(round.getMilestone().getState()),
                        "Нельзя перевести раунд в черновик, т.к. этап находится в статусе %s", round.getMilestone().getState());
                Preconditions.checkArgument(!round.getParticipants().isEmpty(), "В раунде отсутствуют участники");
                log.debug("Проверка возможности запланировать раунд завершена");
                yield true;
            }
            case START -> {
                log.debug("Проверка возможности старта раунда={}, состояние этапа={}",
                        round.getId(), round.getMilestone().getState());
                Preconditions.checkState(round.getMilestone().getState() == MilestoneState.IN_PROGRESS,
                        "Нельзя стартовать раунд, т.к. этап находится в статусе %s", round.getMilestone().getState());
                Preconditions.checkArgument(!round.getParticipants().isEmpty(), "В раунде отсутствуют участники");
                log.debug("Старт раунда разрешен");
                yield true;
            }
            case MARK_READY -> {
                log.debug("Проверка возможности подтверждения раунда={}", round.getId());

                if (round.getMilestone().getState() != MilestoneState.IN_PROGRESS) {
                    log.debug("Подтверждение невозможно: этап не в состоянии IN_PROGRESS, текущее состояние={}",
                            round.getMilestone().getState());
                    yield false;
                }

                List<JudgeRoundStatusEntity> judgeRoundStatuses = judgeRoundStatusRepository.findByRoundId(round.getId());
                Set<Long> acceptedJudgeIds = judgeRoundStatuses.stream()
                        .filter(jrs -> jrs.getStatus() == JudgeRoundStatus.READY)
                        .map(jrs -> jrs.getJudge().getUser().getId())
                        .collect(Collectors.toSet());

                Set<Long> requiredJudgeIds = round.getMilestone().getActivity().getActivityUsers()
                        .stream()
                        .filter(ua -> ua.getPosition().isJudge())
                        .map(ua -> ua.getUser().getId())
                        .collect(Collectors.toSet());

                log.debug("Готовых судей={}, требуемых судей={}", acceptedJudgeIds.size(), requiredJudgeIds.size());
                log.debug("ID готовых судей: {}", acceptedJudgeIds);
                log.debug("ID требуемых судей: {}", requiredJudgeIds);

                boolean allJudgesReady = acceptedJudgeIds.containsAll(requiredJudgeIds);
                log.debug("Все судьи готовы: {}", allJudgesReady);
                yield allJudgesReady;
            }
            case COMPLETE -> {
                log.debug("Проверка возможности завершить раунд={}, состояние этапа={}",
                        round.getId(), round.getMilestone().getState());
                Preconditions.checkState(Set.of(MilestoneState.IN_PROGRESS, MilestoneState.SUMMARIZING).contains(round.getMilestone().getState()),
                        "Нельзя завершить раунд, т.к. этап находится в статусе %s", round.getMilestone().getState());
                log.debug("Проверка возможности завершить раунд завершена");
                yield true;
            }
        };
    }

    @Override
    public Optional<RoundState> getNextState(RoundState currentState, RoundEvent event) {
        RoundState nextState =  switch (currentState) {
            case DRAFT -> switch (event) {
                case DRAFT -> currentState;
                case PLAN -> RoundState.PLANNED;
                default -> null;
            };
            case PLANNED -> switch (event) {
                case DRAFT -> RoundState.DRAFT;
                case PLAN -> currentState;
                case START -> RoundState.IN_PROGRESS;
                default -> null;
            };
            case IN_PROGRESS -> switch (event) {
                case DRAFT -> RoundState.DRAFT;
                case START -> currentState;
                case MARK_READY -> RoundState.READY;
                default -> null;
            };
            case READY -> switch (event) {
                case START -> RoundState.IN_PROGRESS;
                case MARK_READY -> currentState;
                case COMPLETE -> RoundState.COMPLETED;
                default -> null;
            };
            case COMPLETED -> switch (event) {
                case START -> RoundState.IN_PROGRESS;
                case COMPLETE -> currentState;
                default -> null;
            };
        };
        return Optional.ofNullable(nextState);
    }

}
