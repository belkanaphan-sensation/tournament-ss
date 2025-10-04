package org.bn.sensation.core.participant.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRoundResultRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantRoundResultDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRoundResultRequest;

public interface ParticipantRoundResultService extends BaseCrudService<
        ParticipantRoundResultEntity,
        ParticipantRoundResultDto,
        CreateParticipantRoundResultRequest,
        UpdateParticipantRoundResultRequest> {

    /**
     * Найти результаты раунда по ID раунда
     */
    List<ParticipantRoundResultDto> findByRoundId(Long roundId);

    /**
     * Найти результаты раунда по ID этапа
     */
    List<ParticipantRoundResultDto> findByMilestoneId(Long milestoneId);

    /**
     * Найти результаты раунда по ID участника
     */
    List<ParticipantRoundResultDto> findByParticipantId(Long participantId);

    /**
     * Найти результаты раунда по ID судьи
     */
    List<ParticipantRoundResultDto> findByActivityUserId(Long activityUserId);
}
