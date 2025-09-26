package org.bn.sensation.core.participant.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;

public interface ParticipantService extends BaseCrudService<
        ParticipantEntity,
        ParticipantDto,
        CreateParticipantRequest,
        UpdateParticipantRequest> {

    ParticipantDto assignParticipantToRound(Long participantId, Long roundId);

    List<ParticipantDto> findByRoundId(Long roundId);
}
