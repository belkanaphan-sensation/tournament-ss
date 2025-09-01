package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface MilestoneDtoMapper extends BaseDtoMapper<MilestoneEntity, MilestoneDto> {

    @Override
    MilestoneEntity toEntity(MilestoneDto dto);

    @Override
    MilestoneDto toDto(MilestoneEntity entity);
}
