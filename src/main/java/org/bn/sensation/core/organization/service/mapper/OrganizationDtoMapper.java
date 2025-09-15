package org.bn.sensation.core.organization.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.OrganizationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface OrganizationDtoMapper extends BaseDtoMapper<OrganizationEntity, OrganizationDto> {

    @Override
    OrganizationEntity toEntity(OrganizationDto dto);

    @Override
    @Mapping(target = "users", source = "users")
    OrganizationDto toDto(OrganizationEntity entity);
}
