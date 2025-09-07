package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.bn.sensation.core.occasion.service.mapper.OccasionDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OccasionServiceImpl implements OccasionService {

    private final OccasionRepository occasionRepository;
    private final OccasionDtoMapper occasionDtoMapper;
    private final OrganizationRepository organizationRepository;

    @Override
    public BaseRepository<OccasionEntity> getRepository() {
        return occasionRepository;
    }

    @Override
    public BaseDtoMapper<OccasionEntity, OccasionDto> getMapper() {
        return occasionDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OccasionDto> findAll(Pageable pageable) {
        return occasionRepository.findAll(pageable).map(occasionDtoMapper::toDto);
    }

    @Override
    @Transactional
    public OccasionDto create(CreateOccasionRequest request) {
        // Validate organization exists
        OrganizationEntity organization = null;
        if (request.getOrganizationId() != null) {
            organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + request.getOrganizationId()));
        }

        // Create occasion entity
        OccasionEntity occasion = OccasionEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organization(organization)
                .build();

        OccasionEntity saved = occasionRepository.save(occasion);
        return occasionDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OccasionDto update(Long id, UpdateOccasionRequest request) {
        OccasionEntity occasion = occasionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Occasion not found with id: " + id));

        // Update occasion fields
        if (request.getName() != null) occasion.setName(request.getName());
        if (request.getDescription() != null) occasion.setDescription(request.getDescription());
        if (request.getStartDate() != null) occasion.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) occasion.setEndDate(request.getEndDate());

        // Update organization
        if (request.getOrganizationId() != null) {
            OrganizationEntity organization = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + request.getOrganizationId()));
            occasion.setOrganization(organization);
        }

        OccasionEntity saved = occasionRepository.save(occasion);
        return occasionDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!occasionRepository.existsById(id)) {
            throw new IllegalArgumentException("Occasion not found with id: " + id);
        }
        occasionRepository.deleteById(id);
    }
}
