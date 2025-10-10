package org.bn.sensation.core.milestone.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRuleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateMilestoneRuleRequestMapper extends BaseDtoMapper<MilestoneRuleEntity, CreateMilestoneRuleRequest> {

    @Override
    @Mapping(target = "milestone", source = "milestoneId")
    MilestoneRuleEntity toEntity(CreateMilestoneRuleRequest dto);

    @Override
    @Mapping(target = "milestoneId", source = "milestone.id")
    CreateMilestoneRuleRequest toDto(MilestoneRuleEntity entity);

    default MilestoneEntity map(Long milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setId(milestoneId);
        return milestone;
    }
}
