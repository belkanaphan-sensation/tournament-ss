package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.service.dto.MilestoneRuleDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface MilestoneRuleDtoMapper extends BaseDtoMapper<MilestoneRuleEntity, MilestoneRuleDto> {

    @Override
    MilestoneRuleEntity toEntity(MilestoneRuleDto dto);

    @Override
    MilestoneRuleDto toDto(MilestoneRuleEntity entity);
}
