package org.bn.sensation.core.organization.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.repository.OrganizationRepository;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.mapper.OrganizationDtoMapper;

public class OrganizationServiceImpl extends BaseService<OrganizationEntity, OrganizationDto>
        implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationDtoMapper organizationDtoMapper;

    public OrganizationServiceImpl(
            OrganizationRepository organizationRepository, OrganizationDtoMapper organizationDtoMapper) {
        super(organizationRepository, organizationDtoMapper);
        this.organizationRepository = organizationRepository;
        this.organizationDtoMapper = organizationDtoMapper;
    }
}
