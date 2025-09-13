package org.bn.sensation.core.occasion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.OccasionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface OccasionDtoMapper extends BaseDtoMapper<OccasionEntity, OccasionDto> {

    @Override
    OccasionEntity toEntity(OccasionDto dto);

    @Override
    @Mapping(target = "activities", source = "activities")
    OccasionDto toDto(OccasionEntity entity);
}
