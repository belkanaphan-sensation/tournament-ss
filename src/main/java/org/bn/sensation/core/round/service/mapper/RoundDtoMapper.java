package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface RoundDtoMapper extends BaseDtoMapper<RoundEntity, RoundDto> {

    @Override
    RoundEntity toEntity(RoundDto dto);

    @Override
    RoundDto toDto(RoundEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(RoundEntity entity);
}
