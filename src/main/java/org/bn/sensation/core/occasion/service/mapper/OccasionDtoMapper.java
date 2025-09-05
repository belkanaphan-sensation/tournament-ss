package org.bn.sensation.core.occasion.service.mapper;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.bn.sensation.core.organization.service.mapper.OrganizationDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {OrganizationDtoMapper.class})
public interface OccasionDtoMapper extends BaseDtoMapper<OccasionEntity, OccasionDto> {

    @Override
    OccasionEntity toEntity(OccasionDto dto);

    @Override
    OccasionDto toDto(OccasionEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(OccasionEntity entity);
}
