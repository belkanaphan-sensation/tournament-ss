package org.bn.sensation.core.contestant.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class, ContestParticipantDtoMapper.class})
public interface ContestantDtoMapper extends BaseDtoMapper<ContestantEntity, ContestantDto> {

    @Override
    ContestantEntity toEntity(ContestantDto dto);

    @Override
    ContestantDto toDto(ContestantEntity entity);
}
