package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface MilestoneDtoMapper extends BaseDtoMapper<MilestoneEntity, MilestoneDto> {

    @Override
    MilestoneEntity toEntity(MilestoneDto dto);

    @Override
    @Mapping(target = "rounds", source = "rounds")
    MilestoneDto toDto(MilestoneEntity entity);
}
