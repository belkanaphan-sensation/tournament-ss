package org.bn.sensation.core.organization.service.mapper;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface OrganizationDtoMapper extends BaseDtoMapper<OrganizationEntity, OrganizationDto> {

    @Override
    OrganizationEntity toEntity(OrganizationDto dto);

    @Override
    OrganizationDto toDto(OrganizationEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(OrganizationEntity entity);
}
