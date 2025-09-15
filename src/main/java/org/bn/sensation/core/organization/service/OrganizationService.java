package org.bn.sensation.core.organization.service;

import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;

public interface OrganizationService extends BaseCrudService<
        OrganizationEntity,
        OrganizationDto,
        CreateOrganizationRequest,
        UpdateOrganizationRequest> {
}
