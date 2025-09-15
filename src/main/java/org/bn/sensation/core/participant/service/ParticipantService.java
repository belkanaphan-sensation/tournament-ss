package org.bn.sensation.core.participant.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParticipantService extends BaseService<ParticipantEntity, ParticipantDto> {

    // CRUD operations
    Page<ParticipantDto> findAll(Pageable pageable);

    ParticipantDto create(CreateParticipantRequest request);

    ParticipantDto update(Long id, UpdateParticipantRequest request);

    void deleteById(Long id);

    ParticipantDto assignParticipantToRound(Long participantId, Long roundId);
}
