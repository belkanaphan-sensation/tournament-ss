package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface RoundDtoMapper extends BaseDtoMapper<RoundEntity, RoundDto> {

    @Override
    RoundEntity toEntity(RoundDto dto);

    @Override
    RoundDto toDto(RoundEntity entity);
}
