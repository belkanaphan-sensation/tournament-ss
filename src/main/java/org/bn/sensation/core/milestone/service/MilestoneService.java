package org.bn.sensation.core.milestone.service;

import java.util.List;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.PrepareRoundsRequest;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface MilestoneService extends BaseCrudService<
        MilestoneEntity,
        MilestoneDto,
        CreateMilestoneRequest,
        UpdateMilestoneRequest> {

    List<MilestoneDto> findByActivityId(@NotNull Long id);

    List<MilestoneDto> findByActivityIdInLifeStates(@NotNull Long id);

    void draftMilestone(Long id);

    void planMilestone(Long id);

    List<RoundDto> prepareRounds(Long milestoneId, @Valid PrepareRoundsRequest request);

    void startMilestone(Long id);

    List<MilestoneResultDto> sumUpMilestone(Long id);

    void completeMilestone(Long milestoneId, List<UpdateMilestoneResultRequest> request);
}
