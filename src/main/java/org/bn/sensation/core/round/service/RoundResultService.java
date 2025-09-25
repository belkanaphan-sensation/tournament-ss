package org.bn.sensation.core.round.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundResultRequest;

public interface RoundResultService extends BaseCrudService<
        RoundResultEntity,
        RoundResultDto,
        CreateRoundResultRequest,
        UpdateRoundResultRequest> {

    /**
     * Найти результаты раунда по ID раунда
     */
    List<RoundResultDto> findByRoundId(Long roundId);

    /**
     * Найти результаты раунда по ID этапа
     */
    List<RoundResultDto> findByMilestoneId(Long milestoneId);

    /**
     * Найти результаты раунда по ID участника
     */
    List<RoundResultDto> findByParticipantId(Long participantId);

    /**
     * Найти результаты раунда по ID судьи
     */
    List<RoundResultDto> findByActivityUserId(Long activityUserId);
}
