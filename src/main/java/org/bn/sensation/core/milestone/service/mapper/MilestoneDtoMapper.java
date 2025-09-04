package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.activity.service.mapper.ActivityDtoMapper;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {ActivityDtoMapper.class})
public interface MilestoneDtoMapper extends BaseDtoMapper<MilestoneEntity, MilestoneDto> {

    @Override
    MilestoneEntity toEntity(MilestoneDto dto);

    @Override
    MilestoneDto toDto(MilestoneEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(MilestoneEntity entity);
}
