package org.bn.sensation.core.judgemilestonestatus.service;

import java.util.List;

import org.bn.sensation.core.judgemilestonestatus.dto.JudgeMilestoneStatusDto;

public interface JudgeMilestoneStatusCacheService {

    List<JudgeMilestoneStatusDto> getAllJudgesStatusForMilestone(Long milestoneId);

    void invalidateForMilestone(Long milestoneId);
}
