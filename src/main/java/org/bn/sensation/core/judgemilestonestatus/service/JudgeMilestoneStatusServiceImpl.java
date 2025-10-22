package org.bn.sensation.core.judgemilestonestatus.service;

import java.util.List;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatusEntity;
import org.bn.sensation.core.judgemilestonestatus.repository.JudgeMilestoneStatusRepository;
import org.bn.sensation.core.judgemilestonestatus.service.dto.JudgeMilestoneStatusDto;
import org.bn.sensation.core.judgemilestonestatus.service.mapper.JudgeMilestoneStatusDtoMapper;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        log.info("Изменение статуса этапа судьи: этап={}, статус={}, пользователь={}",
                milestoneId, judgeMilestoneStatus, currentUser.getSecurityUser().getId());

        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        Preconditions.checkArgument(judgeMilestoneStatus != null, "Статус не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        ActivityUserEntity activityUser = getActivityUser(milestone);

        log.debug("Найден судья={} для этапа={}, состояние этапа={}",
                activityUser.getId(), milestoneId, milestone.getState());

        Preconditions.checkState(milestone.getState() == MilestoneState.IN_PROGRESS,
                "Статус этапа %s. Не может быть принят или отменен судьей", milestone.getState());

        List<Long> roundIds = milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList();
        log.debug("Проверка готовности {} раундов для этапа={}", roundIds.size(), milestoneId);

        if (!canChange(activityUser.getId(), roundIds, judgeMilestoneStatus)) {
            log.warn("Судья={} не может изменить статус этапа={} на {} - не все раунды готовы",
                    activityUser.getId(), milestoneId, judgeMilestoneStatus);
            throw new IllegalStateException("Не все результаты раундов готовы");
        }

        JudgeMilestoneStatusDto result = changeMilestoneStatus(milestone, activityUser, judgeMilestoneStatus);
        log.info("Статус этапа судьи успешно изменен: этап={}, статус={}", milestoneId, judgeMilestoneStatus);
        return result;
    }

    @Override
    @Transactional
    public JudgeMilestoneStatusDto changeMilestoneStatus(MilestoneEntity milestone, ActivityUserEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus) {
        JudgeMilestoneStatusEntity judgeMilestoneStatusEntity = judgeMilestoneStatusRepository.findByMilestoneIdAndJudgeId(milestone.getId(), activityUser.getId())
                .orElse(JudgeMilestoneStatusEntity.builder().milestone(milestone).judge(activityUser).build());
        judgeMilestoneStatusEntity.setStatus(judgeMilestoneStatus);
        return judgeMilestoneStatusDtoMapper.toDto(judgeMilestoneStatusRepository.save(judgeMilestoneStatusEntity));
    }


    @Override
    @Transactional(readOnly = true)
    public boolean allRoundsReady(Long milestoneId) {
        log.debug("Проверка готовности всех раундов для этапа={}", milestoneId);

        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        ActivityUserEntity activityUser = getActivityUser(milestone);

        List<Long> roundIds = milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList();
        boolean allReady = canChange(activityUser.getId(), roundIds, JudgeMilestoneStatus.READY);

        log.debug("Все раунды готовы для этапа={}: {}", milestoneId, allReady);
        return allReady;
    }

    private ActivityUserEntity getActivityUser(MilestoneEntity milestone) {
        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                milestone.getActivity(), userId, ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge());
        return activityUser;
    }

    private boolean canChange(Long activityUserId, List<Long> roundIds, JudgeMilestoneStatus judgeMilestoneStatus) {
        log.debug("Проверка возможности изменения статуса: судья={}, раундов={}, статус={}",
                activityUserId, roundIds.size(), judgeMilestoneStatus);

        if (judgeMilestoneStatus == JudgeMilestoneStatus.READY) {
            int readyRounds = judgeRoundStatusRepository.countByJudgeIdAndStatusAndRoundIdIn(activityUserId, JudgeRoundStatus.READY, roundIds);
            boolean canChange = readyRounds == roundIds.size();

            log.debug("Готовых раундов={}, всего раундов={}, может изменить={}",
                    readyRounds, roundIds.size(), canChange);

            return canChange;
        }
        log.debug("Статус не READY, изменение разрешено");
        return true;
    }
}
