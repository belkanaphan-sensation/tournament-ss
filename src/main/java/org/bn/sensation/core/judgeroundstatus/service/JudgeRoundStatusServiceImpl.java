package org.bn.sensation.core.judgeroundstatus.service;

import java.util.List;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.judgeroundstatus.service.dto.JudgeRoundStatusDto;
import org.bn.sensation.core.judgeroundstatus.service.mapper.JudgeRoundStatusDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeRoundStatusServiceImpl implements JudgeRoundStatusService {

    private final CurrentUser currentUser;
    private final JudgeRoundStatusDtoMapper judgeRoundStatusDtoMapper;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final MilestoneRepository milestoneRepository;
    private final RoundRepository roundRepository;
    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;

    @Override
    public BaseRepository<JudgeRoundStatusEntity> getRepository() {
        return judgeRoundStatusRepository;
    }

    @Override
    public BaseDtoMapper<JudgeRoundStatusEntity, JudgeRoundStatusDto> getMapper() {
        return judgeRoundStatusDtoMapper;
    }

    @Override
    @Transactional
    public JudgeRoundStatusDto markNotReady(Long roundId) {
        log.info("Откат статуса раунда судьи: раунд={}, судья={}", roundId, currentUser.getSecurityUser().getId());
        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        RoundEntity round = roundRepository.getByIdWithUserOrThrow(roundId);
        ActivityUserEntity activityUser = getActivityUser(round.getMilestone(), currentUser.getSecurityUser().getId());
        JudgeRoundStatusEntity status = judgeRoundStatusRepository
                .getByRoundIdAndJudgeIdOrThrow(roundId, activityUser.getId());
        status.setStatus(JudgeRoundStatus.NOT_READY);
        JudgeRoundStatusEntity saved = judgeRoundStatusRepository.save(status);

        judgeMilestoneStatusCacheService.invalidateForMilestone(round.getMilestone().getId());
        log.debug("Инвалидирован кэш статуса этапа milestoneId={} после отката статуса судьи", round.getMilestone().getId());

        return judgeRoundStatusDtoMapper.toDto(saved);
    }

    @Override
    public JudgeRoundStatus getRoundStatusForCurrentUser(Long roundId) {
        RoundEntity round = roundRepository.getByIdWithUserOrThrow(roundId);
        ActivityUserEntity activityUser = getActivityUser(round.getMilestone(), currentUser.getSecurityUser().getId());
        return judgeRoundStatusRepository.findByRoundIdAndJudgeId(roundId, activityUser.getId()).map(JudgeRoundStatusEntity::getStatus).orElse(null);
    }

    @Override
    public List<JudgeRoundStatusDto> getByMilestoneIdForCurrentUser(Long milestoneId) {
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        ActivityUserEntity activityAssignment = getActivityUser(milestone, currentUser.getSecurityUser().getId());

        return judgeRoundStatusRepository.findByMilestoneIdAndJudgeId(milestoneId, activityAssignment.getId())
                .stream()
                .map(judgeRoundStatusDtoMapper::toDto)
                .toList();
    }

    private ActivityUserEntity getActivityUser(MilestoneEntity milestone, Long userId) {
        return ActivityUserUtil.getFromActivity(
                milestone.getActivity(), userId, ua ->
                        ua.getUser()
                                .getId()
                                .equals(userId)
                                && ua.getPosition().isJudge());
    }
}
