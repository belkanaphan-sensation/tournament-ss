package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.bn.sensation.core.activity.service.dto.CreateActivityRequest;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.activity.service.mapper.ActivityDtoMapper;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityDtoMapper activityDtoMapper;
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
        OccasionEntity occasion = null;
        if (request.getOccasionId() != null) {
            occasion = occasionRepository.findById(request.getOccasionId())
                    .orElseThrow(() -> new IllegalArgumentException("Occasion not found with id: " + request.getOccasionId()));
        }

        // Create activity entity
        ActivityEntity activity = ActivityEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .address(request.getAddress() != null ? Address.builder()
                        .country(request.getAddress().getCountry())
                        .city(request.getAddress().getCity())
                        .streetName(request.getAddress().getStreetName())
                        .streetNumber(request.getAddress().getStreetNumber())
                        .comment(request.getAddress().getComment())
                        .build() : null)
                .occasion(occasion)
                .build();

        ActivityEntity saved = activityRepository.save(activity);
        return activityDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ActivityDto update(Long id, UpdateActivityRequest request) {
        ActivityEntity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + id));

        // Update activity fields
        if (request.getName() != null) activity.setName(request.getName());
        if (request.getDescription() != null) activity.setDescription(request.getDescription());
        if (request.getStartDateTime() != null) activity.setStartDateTime(request.getStartDateTime());
        if (request.getEndDateTime() != null) activity.setEndDateTime(request.getEndDateTime());

        // Update address
        if (request.getAddress() != null) {
            Address address = activity.getAddress();
            if (address == null) {
                address = Address.builder().build();
            }

            if (request.getAddress().getCountry() != null) address.setCountry(request.getAddress().getCountry());
            if (request.getAddress().getCity() != null) address.setCity(request.getAddress().getCity());
            if (request.getAddress().getStreetName() != null) address.setStreetName(request.getAddress().getStreetName());
            if (request.getAddress().getStreetNumber() != null) address.setStreetNumber(request.getAddress().getStreetNumber());
            if (request.getAddress().getComment() != null) address.setComment(request.getAddress().getComment());

            activity.setAddress(address);
        }

        // Update occasion
        if (request.getOccasionId() != null) {
            OccasionEntity occasion = occasionRepository.findById(request.getOccasionId())
                    .orElseThrow(() -> new IllegalArgumentException("Occasion not found with id: " + request.getOccasionId()));
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
