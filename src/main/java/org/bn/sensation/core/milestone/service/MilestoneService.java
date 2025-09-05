package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MilestoneService extends BaseService<MilestoneEntity, MilestoneDto> {

    // CRUD operations
    Page<MilestoneDto> findAll(Pageable pageable);

    MilestoneDto create(CreateMilestoneRequest request);

    MilestoneDto update(Long id, UpdateMilestoneRequest request);

    void deleteById(Long id);
}
