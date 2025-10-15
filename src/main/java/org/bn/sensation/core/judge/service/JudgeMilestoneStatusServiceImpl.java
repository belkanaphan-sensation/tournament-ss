package org.bn.sensation.core.judge.service;

import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatusEntity;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judge.repository.JudgeMilestoneStatusRepository;
import org.bn.sensation.core.judge.service.dto.JudgeMilestoneStatusDto;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.judge.service.mapper.JudgeMilestoneStatusDtoMapper;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.judge.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JudgeMilestoneStatusServiceImpl implements JudgeMilestoneStatusService {

    private final CurrentUser currentUser;
    private final JudgeMilestoneStatusDtoMapper judgeMilestoneStatusDtoMapper;
    private final JudgeMilestoneStatusRepository judgeMilestoneStatusRepository;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final MilestoneRepository milestoneRepository;

    @Override
    public BaseRepository<JudgeMilestoneStatusEntity> getRepository() {
        return judgeMilestoneStatusRepository;
    }

    @Override
    public BaseDtoMapper<JudgeMilestoneStatusEntity, JudgeMilestoneStatusDto> getMapper() {
        return judgeMilestoneStatusDtoMapper;
    }

    @Override
    @Transactional
    public JudgeMilestoneStatusDto changeMilestoneStatus(Long milestoneId, JudgeMilestoneStatus judgeMilestoneStatus) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        Preconditions.checkArgument(judgeMilestoneStatus != null, "Статус не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId).orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = getActivityUser(milestoneId, milestone);
        Preconditions.checkState(milestone.getState() == MilestoneState.IN_PROGRESS,
                "Статус этапа %s. Не может быть принят или отменен судьей", milestone.getState());

        if (!canChange(activityUser.getId(), milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList(), judgeMilestoneStatus)) {
            throw new IllegalStateException("Не все результаты раундов готовы");
        }

        return changeMilestoneStatus(milestone, activityUser, judgeMilestoneStatus);
    }

    @Override
    @Transactional
    public JudgeMilestoneStatusDto changeMilestoneStatus(MilestoneEntity milestone, UserActivityAssignmentEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus) {
        JudgeMilestoneStatusEntity judgeMilestoneStatusEntity = judgeMilestoneStatusRepository.findByMilestoneIdAndJudgeId(milestone.getId(), activityUser.getId())
                .orElse(JudgeMilestoneStatusEntity.builder().milestone(milestone).judge(activityUser).build());
        judgeMilestoneStatusEntity.setStatus(judgeMilestoneStatus);
        return judgeMilestoneStatusDtoMapper.toDto(judgeMilestoneStatusRepository.save(judgeMilestoneStatusEntity));
    }


    @Override
    @Transactional(readOnly = true)
    public boolean allRoundsReady(Long milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId).orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = getActivityUser(milestoneId, milestone);

        return canChange(activityUser.getId(), milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList(), JudgeMilestoneStatus.READY);
    }

    private UserActivityAssignmentEntity getActivityUser(Long milestoneId, MilestoneEntity milestone) {
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к этапу с id: %s".formatted(currentUser.getSecurityUser().getId(), milestoneId)));
        return activityUser;
    }

    private boolean canChange(Long activityUserId, List<Long> roundIds, JudgeMilestoneStatus judgeMilestoneStatus) {
        if (judgeMilestoneStatus == JudgeMilestoneStatus.READY) {
            int readyRounds = judgeRoundStatusRepository.countByJudgeIdAndStatusAndRoundIdIn(activityUserId, JudgeRoundStatus.READY, roundIds);
            return readyRounds == roundIds.size();
        }
        return true;
    }
}
