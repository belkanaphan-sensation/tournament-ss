package org.bn.sensation.core.judgemilestonestatus.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatusEntity;
import org.bn.sensation.core.judgemilestonestatus.service.dto.JudgeMilestoneStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface JudgeMilestoneStatusDtoMapper extends BaseDtoMapper<JudgeMilestoneStatusEntity, JudgeMilestoneStatusDto> {

    @Mapping(target = "judge", ignore = true)
    @Mapping(target = "milestone", ignore = true)
    @Override
    JudgeMilestoneStatusEntity toEntity(JudgeMilestoneStatusDto dto);

    @Override
    JudgeMilestoneStatusDto toDto(JudgeMilestoneStatusEntity entity);
}
