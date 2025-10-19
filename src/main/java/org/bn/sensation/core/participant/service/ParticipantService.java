package org.bn.sensation.core.participant.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.RoundParticipantsDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;

public interface ParticipantService extends BaseCrudService<
        ParticipantEntity,
        ParticipantDto,
        CreateParticipantRequest,
        UpdateParticipantRequest> {

    ParticipantDto assignParticipantToRound(Long participantId, Long roundId);

    ParticipantDto removeParticipantFromRound(Long participantId, Long roundId);

    ParticipantDto assignParticipantToMilestone(Long participantId, Long milestoneId);

    ParticipantDto removeParticipantFromMilestone(Long participantId, Long milestoneId);

    List<ParticipantDto> findByRoundId(Long roundId);

    List<ParticipantDto> getByRoundByRoundIdForCurrentUser(Long roundId);

    List<RoundParticipantsDto> getByRoundByMilestoneIdForCurrentUser(Long milestoneId);
}
