package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface ParticipantDtoMapper extends BaseDtoMapper<ParticipantEntity, ParticipantDto> {

    @Override
    ParticipantEntity toEntity(ParticipantDto dto);

    @Override
    @Mapping(target = "rounds", source = "rounds")
    @Mapping(target = "milestones", source = "milestones")
    ParticipantDto toDto(ParticipantEntity entity);
}
