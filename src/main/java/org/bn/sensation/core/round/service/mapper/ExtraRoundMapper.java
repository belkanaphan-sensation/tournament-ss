package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.round.entity.ExtraRoundEntity;
import org.bn.sensation.core.round.service.dto.ExtraRoundDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface ExtraRoundMapper extends BaseDtoMapper<ExtraRoundEntity, ExtraRoundDto> {

    ExtraRoundDto toDto(ExtraRoundEntity extraRound);

    @Mapping(target = "milestone", ignore = true)
    @Mapping(target = "participants", ignore = true)
    ExtraRoundEntity toEntity(ExtraRoundDto request);
}
