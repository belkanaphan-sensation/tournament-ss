package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.round.entity.JudgeRoundEntity;
import org.bn.sensation.core.round.service.dto.JudgeRoundDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface JudgeRoundMapper extends BaseDtoMapper<JudgeRoundEntity, JudgeRoundDto> {

    JudgeRoundDto toDto(JudgeRoundEntity extraRound);

    @Mapping(target = "judge", ignore = true)
    @Mapping(target = "round", ignore = true)
    JudgeRoundEntity toEntity(JudgeRoundDto request);
}
