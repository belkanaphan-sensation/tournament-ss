package org.bn.sensation.core.judgemilestonestatus.service;

import java.time.LocalDateTime;
import java.util.List;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.judgemilestonestatus.dto.JudgeMilestoneStatusDto;
import org.bn.sensation.core.judgemilestonestatus.model.JudgeMilestoneStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeMilestoneStatusCacheServiceImpl implements JudgeMilestoneStatusCacheService {

    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final MilestoneRepository milestoneRepository;
    private final EntityLinkMapper entityLinkMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "judgeMilestoneStatus", key = "#milestoneId", sync = true)
    public List<JudgeMilestoneStatusDto> getAllJudgesStatusForMilestone(Long milestoneId) {
        log.trace("Получение статусов всех судей для этапа: milestoneId={}", milestoneId);
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");

        MilestoneEntity milestone = milestoneRepository.getByIdWithActivityUsersOrThrow(milestoneId);
        EntityLinkDto milestoneLink = entityLinkMapper.toEntityLinkDto(milestone);

        List<ActivityUserEntity> judges = milestone.getActivity().getActivityUsers()
                .stream()
                .filter(au -> au.getPosition().isJudge())
                .toList();

        log.trace("Найдено {} судей для этапа={}", judges.size(), milestoneId);

        LocalDateTime calculatedAt = LocalDateTime.now();

        return judges.stream()
                .map(judge -> {
                    Long notReadyCount = judgeRoundStatusRepository.countNotEqualStatusForMilestoneIdAndJudgeId(
                            milestoneId, judge.getId(), JudgeRoundStatus.READY);
                    JudgeMilestoneStatus status = notReadyCount == 0L ? JudgeMilestoneStatus.READY : JudgeMilestoneStatus.NOT_READY;
                    EntityLinkDto judgeLink = entityLinkMapper.toEntityLinkDto(judge);

                    return JudgeMilestoneStatusDto.builder()
                            .judge(judgeLink)
                            .milestone(milestoneLink)
                            .status(status)
                            .calculatedAt(calculatedAt)
                            .build();
                })
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "judgeMilestoneStatus", key = "#milestoneId")
    public void invalidateForMilestone(Long milestoneId) {
        log.trace("Инвалидация кэша статуса судей для milestoneId={}", milestoneId);
    }
}
