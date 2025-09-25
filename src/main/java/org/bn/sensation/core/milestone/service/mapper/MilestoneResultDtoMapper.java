package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface MilestoneResultDtoMapper extends BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> {

    @Override
    MilestoneResultEntity toEntity(MilestoneResultDto dto);

    @Override
    MilestoneResultDto toDto(MilestoneResultEntity entity);
}
