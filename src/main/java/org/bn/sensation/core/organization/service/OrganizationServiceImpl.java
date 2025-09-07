package org.bn.sensation.core.organization.service;

import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;
import org.bn.sensation.core.organization.service.mapper.OrganizationDtoMapper;
import org.bn.sensation.core.organization.service.mapper.CreateOrganizationRequestMapper;
import org.bn.sensation.core.organization.service.mapper.UpdateOrganizationRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationDtoMapper organizationDtoMapper;
    private final CreateOrganizationRequestMapper createOrganizationRequestMapper;
    private final UpdateOrganizationRequestMapper updateOrganizationRequestMapper;

    @Override
    public BaseRepository<OrganizationEntity> getRepository() {
        return organizationRepository;
    }

    @Override
    public BaseDtoMapper<OrganizationEntity, OrganizationDto> getMapper() {
        return organizationDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationDto> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationDtoMapper::toDto);
    }

    @Override
    @Transactional
    public OrganizationDto create(CreateOrganizationRequest request) {
        // Check if organization with same name already exists
        organizationRepository.findByName(request.getName())
                .ifPresent(org -> {
                    throw new IllegalArgumentException("Organization with name already exists: " + request.getName());
                });

        // Check if email already exists
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            organizationRepository.findByEmail(request.getEmail())
                    .ifPresent(org -> {
                        throw new IllegalArgumentException("Organization with email already exists: " + request.getEmail());
                    });
        }

        // Create organization entity
        OrganizationEntity organization = createOrganizationRequestMapper.toEntity(request);

        OrganizationEntity saved = organizationRepository.save(organization);
        return organizationDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrganizationDto update(Long id, UpdateOrganizationRequest request) {
        OrganizationEntity organization = organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with id: " + id));

        // Check if name already exists (if changed)
        if (request.getName() != null && !request.getName().isBlank()
                && !request.getName().equals(organization.getName())) {
            organizationRepository.findByName(request.getName())
                    .ifPresent(org -> {
                        throw new IllegalArgumentException("Organization with name already exists: " + request.getName());
                    });
        }

        // Check if email already exists (if changed)
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(organization.getEmail())) {
            organizationRepository.findByEmail(request.getEmail())
                    .ifPresent(org -> {
                        throw new IllegalArgumentException("Organization with email already exists: " + request.getEmail());
                    });
        }

        // Update organization fields
        updateOrganizationRequestMapper.updateOrganizationFromRequest(request, organization);

        // Update address
        if (request.getAddress() != null) {
            Address address = organization.getAddress();
            if (address == null) {
                address = Address.builder().build();
            }
            updateOrganizationRequestMapper.updateAddressFromRequest(request.getAddress(), address);
        }

        OrganizationEntity saved = organizationRepository.save(organization);
        return organizationDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new IllegalArgumentException("Organization not found with id: " + id);
        }
        organizationRepository.deleteById(id);
    }
}
