package org.bn.sensation.core.judge.service;

import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judge.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.judge.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.judge.repository.JudgeRoundRepository;
import org.bn.sensation.core.judge.service.dto.JudgeRoundDto;
import org.bn.sensation.core.judge.service.mapper.JudgeRoundMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeRoundServiceImpl implements JudgeRoundService{

    private final CurrentUser currentUser;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final JudgeRoundMapper judgeRoundMapper;
    private final JudgeRoundRepository judgeRoundRepository;
    private final RoundRepository roundRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;

    @Override
    public BaseRepository<JudgeRoundEntity> getRepository() {
        return judgeRoundRepository;
    }

    @Override
    public BaseDtoMapper<JudgeRoundEntity, JudgeRoundDto> getMapper() {
        return judgeRoundMapper;
    }

    @Override
    @Transactional
    public JudgeRoundDto changeJudgeRoundStatus(Long roundId, JudgeRoundStatus judgeRoundStatus) {
        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        Preconditions.checkArgument(judgeRoundStatus != null, "Статус не может быть null");

        RoundEntity round = roundRepository.findByIdWithUserAssignments(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + roundId));
        UserActivityAssignmentEntity activityAssignment = round.getMilestone().getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к раунду с id: %s".formatted(currentUser.getSecurityUser().getId(), roundId)));
        Preconditions.checkState(round.getState() == RoundState.IN_PROGRESS,
                "Статус раунда %s. Не может быть принят или отменен судьей", round.getState());

        if (!canChange(judgeRoundStatus, activityAssignment, round)) {
            throw new IllegalStateException("Судья оценил не всех участников");
        }

        return createOrUpdateJudgeRoundStatus(judgeRoundStatus, activityAssignment, round);
    }

    private JudgeRoundDto createOrUpdateJudgeRoundStatus(JudgeRoundStatus judgeRoundStatus, UserActivityAssignmentEntity activityAssignment, RoundEntity round) {
        JudgeRoundEntity judgeRoundEntity = judgeRoundRepository.findByRoundIdAndJudgeId(round.getId(), activityAssignment.getId())
                .orElse(JudgeRoundEntity.builder().round(round).judge(activityAssignment).build());
        judgeRoundEntity.setStatus(judgeRoundStatus);
        return judgeRoundMapper.toDto(judgeRoundRepository.save(judgeRoundEntity));
    }

    private boolean canChange(JudgeRoundStatus judgeRoundStatus, UserActivityAssignmentEntity activityUser, RoundEntity round) {
        if (judgeRoundStatus == JudgeRoundStatus.READY) {
            List<JudgeMilestoneResultEntity> results = judgeMilestoneResultRepository.findByActivityUserId(activityUser.getId());
            if (activityUser.getPartnerSide() == null) {
                return round.getParticipants().size() == results.size();
            } else {
                //TODO добавить смену сторон, если есть правило этапа на смену сторон судей
                long participantsCount = round.getParticipants()
                        .stream()
                        .filter(p -> p.getPartnerSide() == activityUser.getPartnerSide())
                        .count();
                long resultsCount = results.stream()
                        .filter(prr -> prr.getParticipant().getPartnerSide() == activityUser.getPartnerSide())
                        .count();
                return participantsCount == resultsCount;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void changeJudgeRoundStatusIfPossible(Long activityUserId, Long roundId, JudgeRoundStatus judgeRoundStatus) {
        Preconditions.checkArgument(activityUserId != null, "ID судьи не может быть null");
        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        Preconditions.checkArgument(judgeRoundStatus != null, "Статус не может быть null");
        UserActivityAssignmentEntity activityUser = userActivityAssignmentRepository.findById(activityUserId).orElseThrow(EntityNotFoundException::new);
        RoundEntity round = roundRepository.findByIdWithUserAssignments(roundId).orElseThrow(EntityNotFoundException::new);
        Preconditions.checkArgument(round.getMilestone().getActivity().getUserAssignments()
                        .stream()
                        .map(UserActivityAssignmentEntity::getId)
                        .anyMatch(id -> id.equals(activityUser.getId())),
                "Некорректные данные");
        if (round.getState() == RoundState.IN_PROGRESS && canChange(judgeRoundStatus, activityUser, round)) {
            createOrUpdateJudgeRoundStatus(judgeRoundStatus, activityUser, round);
        }
        log.info("Judge round status for activity user {} and round {} changed to {}", activityUserId, roundId, judgeRoundStatus);
    }

}
