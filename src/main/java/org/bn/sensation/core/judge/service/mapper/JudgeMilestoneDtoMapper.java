package org.bn.sensation.core.judge.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.judge.entity.JudgeMilestoneEntity;
import org.bn.sensation.core.judge.service.dto.JudgeMilestoneDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface JudgeMilestoneDtoMapper extends BaseDtoMapper<JudgeMilestoneEntity, JudgeMilestoneDto> {

    @Mapping(target = "judge", ignore = true)
    @Mapping(target = "milestone", ignore = true)
    @Override
    JudgeMilestoneEntity toEntity(JudgeMilestoneDto dto);

    @Override
    JudgeMilestoneDto toDto(JudgeMilestoneEntity entity);
}
