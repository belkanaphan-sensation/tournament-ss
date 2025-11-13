package org.bn.sensation.core.contestant.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.bn.sensation.core.contestant.service.dto.CreateContestantRequest;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;

public interface ContestantService extends BaseService<ContestantEntity, ContestantDto> {

    ContestantDto create(CreateContestantRequest request);

    void deleteById(Long id);

    List<ContestantDto> findByRoundId(Long roundId);

    List<ContestantDto> getByRoundByRoundIdForCurrentUser(Long roundId);

    List<ContestantEntity> createContestants(MilestoneEntity milestone, List<ParticipantEntity> participants);
}
