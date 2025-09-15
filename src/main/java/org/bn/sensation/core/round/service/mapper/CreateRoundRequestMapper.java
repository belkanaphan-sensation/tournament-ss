package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateRoundRequestMapper extends BaseDtoMapper<RoundEntity, CreateRoundRequest> {
    @Override
    @Mapping(target = "milestone", source = "milestoneId")
    RoundEntity toEntity(CreateRoundRequest dto);

    @Override
    @Mapping(target = "milestoneId", source = "milestone.id")
    CreateRoundRequest toDto(RoundEntity entity);

    default MilestoneEntity map(Long milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        MilestoneEntity milestone = new MilestoneEntity();
        milestone.setId(milestoneId);
        return milestone;
    }
}
