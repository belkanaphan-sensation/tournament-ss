package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface RoundResultDtoMapper extends BaseDtoMapper<RoundResultEntity, RoundResultDto> {

    @Override
    RoundResultEntity toEntity(RoundResultDto dto);

    @Override
    RoundResultDto toDto(RoundResultEntity entity);
}
