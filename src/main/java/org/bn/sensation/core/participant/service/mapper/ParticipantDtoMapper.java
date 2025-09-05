package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface ParticipantDtoMapper extends BaseDtoMapper<ParticipantEntity, ParticipantDto> {

    @Override
    ParticipantEntity toEntity(ParticipantDto dto);

    @Override
    ParticipantDto toDto(ParticipantEntity entity);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "number")
    EntityLinkDto toEntityLinkDto(ParticipantEntity entity);
}
