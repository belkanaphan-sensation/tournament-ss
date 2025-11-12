package org.bn.sensation.core.round.service;

import java.util.Optional;
import java.util.Set;

import org.bn.sensation.core.common.service.BaseStateService;
import org.bn.sensation.core.round.statemachine.RoundEvent;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.service.JudgeRoundStatusService;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoundStateServiceImpl implements BaseStateService<RoundEntity, RoundState, RoundEvent> {

    private final RoundRepository roundRepository;
    private final JudgeRoundStatusService judgeRoundStatusService;

    @Override
    @Transactional
    public void saveTransition(RoundEntity round, RoundState state) {
        round.setState(state);
        roundRepository.save(round);
    }

    @Override
    public String canTransition(RoundEntity round, RoundEvent event) {
        log.debug("Проверка возможности перехода раунда={} из состояния={} по событию={}",
                round.getId(), round.getState(), event);
        switch (event) {
            case CLOSE -> {
                log.debug("Проверка возможности подтверждения раунда={}", round.getId());
                if (!Set.of(MilestoneState.IN_PROGRESS, MilestoneState.SUMMARIZING).contains(round.getMilestone().getState())) {
                    return "Нельзя завершить раунд, т.к. этап находится в статусе %s"
                            .formatted(round.getMilestone().getState());
                }
                boolean allJudgesReady = judgeRoundStatusService.getByRoundId(round.getId())
                        .stream().allMatch(jrs -> jrs.getStatus() == JudgeRoundStatus.READY);
                if (!allJudgesReady) {
                    return "Нельзя завершить раунд, т.к. не все судьи проставили результаты";
                }
            }
        }
        return null;
    }

    @Override
    public Optional<RoundState> getNextState(RoundState currentState, RoundEvent event) {
        RoundState nextState = switch (currentState) {
            case OPENED -> switch (event) {
                case CLOSE -> RoundState.CLOSED;
                default -> null;
            };
            case CLOSED -> switch (event) {
                case CLOSE -> currentState;
                default -> null;
            };
        };
        return Optional.ofNullable(nextState);
    }

}
