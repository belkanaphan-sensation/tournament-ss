package org.bn.sensation.core.participant.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.*;

public interface ParticipantService extends BaseCrudService<
        ParticipantEntity,
        ParticipantDto,
        CreateParticipantRequest,
        UpdateParticipantRequest> {

    List<ParticipantDto> findByActivityId(Long activityId);
}
