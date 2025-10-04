package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRoundResultRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateParticipantRoundResultRequestMapper extends BaseDtoMapper<ParticipantRoundResultEntity, UpdateParticipantRoundResultRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoundFromRequest(UpdateParticipantRoundResultRequest request, @MappingTarget ParticipantRoundResultEntity entity);

}
