package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.service.dto.UpdateRoundResultRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateRoundResultRequestMapper extends BaseDtoMapper<RoundResultEntity, UpdateRoundResultRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRoundFromRequest(UpdateRoundResultRequest request, @MappingTarget RoundResultEntity entity);

}
