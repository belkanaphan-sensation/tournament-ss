package org.bn.sensation.core.organization.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.mapper.OrganizationDtoMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationDtoMapper organizationDtoMapper;

    @Override
    public BaseRepository<OrganizationEntity> getRepository() {
        return organizationRepository;
    }

    @Override
    public BaseDtoMapper<OrganizationEntity, OrganizationDto> getMapper() {
        return organizationDtoMapper;
    }
}
