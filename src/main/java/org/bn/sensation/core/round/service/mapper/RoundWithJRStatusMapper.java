package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface RoundWithJRStatusMapper {

    @Mapping(target = "id", source = "roundEntity.id")
    @Mapping(target = "name", source = "roundEntity.name")
    @Mapping(target = "activity", source = "roundEntity.milestone.activity")
    @Mapping(target = "milestone", source = "roundEntity.milestone")
    @Mapping(target = "participants", source = "roundEntity.participants")
    @Mapping(target = "state", source = "roundEntity.state")
    @Mapping(target = "isExtraRound", source = "roundEntity.extraRound")
    @Mapping(target = "judgeRoundStatus", source = "judgeRoundStatusEntity.status", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    RoundWithJRStatusDto toDto(RoundEntity roundEntity, JudgeRoundStatusEntity judgeRoundStatusEntity);
}
