package org.bn.sensation.core.organization.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationService extends BaseService<OrganizationEntity, OrganizationDto> {

    // CRUD operations
    Page<OrganizationDto> findAll(Pageable pageable);

    OrganizationDto create(CreateOrganizationRequest request);

    OrganizationDto update(Long id, UpdateOrganizationRequest request);

    void deleteById(Long id);
}
