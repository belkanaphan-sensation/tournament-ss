package org.bn.sensation.core.judge.service;

import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.judge.entity.JudgeMilestoneEntity;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judge.repository.JudgeMilestoneRepository;
import org.bn.sensation.core.judge.service.dto.JudgeMilestoneDto;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.judge.service.mapper.JudgeMilestoneDtoMapper;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.judge.repository.JudgeRoundRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JudgeMilestoneServiceImpl implements JudgeMilestoneService{

    private final CurrentUser currentUser;
    private final JudgeMilestoneDtoMapper judgeMilestoneDtoMapper;
    private final JudgeMilestoneRepository judgeMilestoneRepository;
    private final JudgeRoundRepository judgeRoundRepository;
    private final MilestoneRepository milestoneRepository;

    @Override
    public BaseRepository<JudgeMilestoneEntity> getRepository() {
        return judgeMilestoneRepository;
    }

    @Override
    public BaseDtoMapper<JudgeMilestoneEntity, JudgeMilestoneDto> getMapper() {
        return judgeMilestoneDtoMapper;
    }

    @Override
    @Transactional
    public JudgeMilestoneDto changeMilestoneStatus(Long milestoneId, JudgeMilestoneStatus judgeMilestoneStatus) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        Preconditions.checkArgument(judgeMilestoneStatus != null, "Статус не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdWithUserAssignments(milestoneId).orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к этапу с id: %s".formatted(currentUser.getSecurityUser().getId(), milestoneId)));
        Preconditions.checkState(milestone.getState() == MilestoneState.IN_PROGRESS,
                "Статус этапа %s. Не может быть принят или отменен судьей", milestone.getState());

        if (!canChange(activityUser.getId(), milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList(), judgeMilestoneStatus)) {
            throw new IllegalStateException("Не все результаты раундов готовы");
        }

        return changeMilestoneStatus(milestone, activityUser, judgeMilestoneStatus);
    }

    @Override
    @Transactional
    public JudgeMilestoneDto changeMilestoneStatus(MilestoneEntity milestone, UserActivityAssignmentEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus) {
        JudgeMilestoneEntity judgeMilestoneEntity = judgeMilestoneRepository.findByMilestoneIdAndJudgeId(milestone.getId(), activityUser.getId())
                .orElse(JudgeMilestoneEntity.builder().milestone(milestone).judge(activityUser).build());
        judgeMilestoneEntity.setStatus(judgeMilestoneStatus);
        return judgeMilestoneDtoMapper.toDto(judgeMilestoneRepository.save(judgeMilestoneEntity));
    }


    @Override
    @Transactional(readOnly = true)
    public boolean allRoundsReady(Long milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findByIdWithUserAssignments(milestoneId).orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к этапу с id: %s".formatted(currentUser.getSecurityUser().getId(), milestoneId)));

        return canChange(activityUser.getId(), milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList(), JudgeMilestoneStatus.READY);
    }

    private boolean canChange(Long activityUserId, List<Long> roundIds, JudgeMilestoneStatus judgeMilestoneStatus) {
        if (judgeMilestoneStatus == JudgeMilestoneStatus.READY) {
            int readyRounds = judgeRoundRepository.countByJudgeIdAndStatusAndRoundIdIn(activityUserId, JudgeRoundStatus.READY, roundIds);
            return readyRounds == roundIds.size();
        }
        return true;
    }
}
