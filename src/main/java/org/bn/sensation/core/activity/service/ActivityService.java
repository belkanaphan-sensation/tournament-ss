package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.constraints.NotNull;

public interface ActivityService extends BaseService<ActivityEntity, ActivityDto> {

    // CRUD operations
    Page<ActivityDto> findAll(Pageable pageable);

    ActivityDto create(CreateActivityRequest request);

    ActivityDto update(Long id, UpdateActivityRequest request);

    void deleteById(Long id);

    Page<ActivityDto> findByOccasionId(@NotNull Long id, Pageable pageable);
}
