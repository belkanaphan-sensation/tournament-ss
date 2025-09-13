package org.bn.sensation.core.organization.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.CreateOrganizationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateOrganizationRequestMapper extends BaseDtoMapper<OrganizationEntity, CreateOrganizationRequest> {
    
    @Override
    @Mapping(target = "address.city", source = "address.city")
    @Mapping(target = "address.streetName", source = "address.streetName")
    @Mapping(target = "address.streetNumber", source = "address.streetNumber")
    @Mapping(target = "users", ignore = true)
    OrganizationEntity toEntity(CreateOrganizationRequest dto);

    @Override
    @Mapping(target = "address.city", source = "address.city")
    @Mapping(target = "address.streetName", source = "address.streetName")
    @Mapping(target = "address.streetNumber", source = "address.streetNumber")
    CreateOrganizationRequest toDto(OrganizationEntity entity);
}
