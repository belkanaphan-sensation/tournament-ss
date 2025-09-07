package org.bn.sensation.core.organization.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface CreateOrganizationRequestMapper extends BaseDtoMapper<OrganizationEntity, CreateOrganizationRequest> {
}
