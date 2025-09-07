package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.MilestoneDtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneDtoMapper milestoneDtoMapper;
    private final ActivityRepository activityRepository;

    @Override
    public BaseRepository<MilestoneEntity> getRepository() {
        return milestoneRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneEntity, MilestoneDto> getMapper() {
        return milestoneDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneDto> findAll(Pageable pageable) {
        return milestoneRepository.findAll(pageable).map(milestoneDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneDto create(CreateMilestoneRequest request) {
        // Validate activity exists
        ActivityEntity activity = null;
        if (request.getActivityId() != null) {
            activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + request.getActivityId()));
        }

        // Create milestone entity
        MilestoneEntity milestone = MilestoneEntity.builder()
                .name(request.getName())
                .activity(activity)
                .build();

        MilestoneEntity saved = milestoneRepository.save(milestone);
        return milestoneDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneDto update(Long id, UpdateMilestoneRequest request) {
        MilestoneEntity milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with id: " + id));

        // Update milestone fields
        if (request.getName() != null) milestone.setName(request.getName());

        // Update activity
        if (request.getActivityId() != null) {
            ActivityEntity activity = activityRepository.findById(request.getActivityId())
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + request.getActivityId()));
            milestone.setActivity(activity);
        }

        MilestoneEntity saved = milestoneRepository.save(milestone);
        return milestoneDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!milestoneRepository.existsById(id)) {
            throw new IllegalArgumentException("Milestone not found with id: " + id);
        }
        milestoneRepository.deleteById(id);
    }
}
