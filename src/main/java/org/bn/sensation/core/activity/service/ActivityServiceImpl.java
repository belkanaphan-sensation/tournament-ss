package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.activity.service.mapper.ActivityDtoMapper;
import org.bn.sensation.core.activity.service.mapper.CreateActivityRequestMapper;
import org.bn.sensation.core.activity.service.mapper.UpdateActivityRequestMapper;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDtoMapper activityDtoMapper;
    private final CreateActivityRequestMapper createActivityRequestMapper;
    private final UpdateActivityRequestMapper updateActivityRequestMapper;
    private final OccasionRepository occasionRepository;

    @Override
    public BaseRepository<ActivityEntity> getRepository() {
        return activityRepository;
    }

    @Override
    public BaseDtoMapper<ActivityEntity, ActivityDto> getMapper() {
        return activityDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityDto> findAll(Pageable pageable) {
        return activityRepository.findAll(pageable).map(activityDtoMapper::toDto);
    }

    @Override
    @Transactional
    public ActivityDto create(CreateActivityRequest request) {
        // Validate occasion exists
        OccasionEntity occasion = occasionRepository.findById(request.getOccasionId())
                .orElseThrow(() -> new EntityNotFoundException("Occasion not found with id: " + request.getOccasionId()));

        // Create activity entity
        ActivityEntity activity = createActivityRequestMapper.toEntity(request);
        activity.setOccasion(occasion);

        ActivityEntity saved = activityRepository.save(activity);
        return activityDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ActivityDto update(Long id, UpdateActivityRequest request) {
        Preconditions.checkArgument(id != null, "Activity id must not be null");

        ActivityEntity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + id));

        // Update activity fields
        updateActivityRequestMapper.updateActivityFromRequest(request, activity);

        // Update address
        if (request.getAddress() != null) {
            Address address = activity.getAddress();
            if (address == null) {
                address = Address.builder().build();
            }
            updateActivityRequestMapper.updateAddressFromRequest(request.getAddress(), address);
        }

        // Update occasion
        if (request.getOccasionId() != null) {
            OccasionEntity occasion = occasionRepository.findById(request.getOccasionId())
                    .orElseThrow(() -> new EntityNotFoundException("Occasion not found with id: " + request.getOccasionId()));
            activity.setOccasion(occasion);
        }

        ActivityEntity saved = activityRepository.save(activity);
        return activityDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new IllegalArgumentException("Activity not found with id: " + id);
        }
        activityRepository.deleteById(id);
    }
}
