package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateRoundRequestMapper extends BaseDtoMapper<RoundEntity, UpdateRoundRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoundFromRequest(UpdateRoundRequest request, @MappingTarget RoundEntity entity);

    default MilestoneEntity map(Long milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setId(milestoneId);
        return milestone;
    }
}
