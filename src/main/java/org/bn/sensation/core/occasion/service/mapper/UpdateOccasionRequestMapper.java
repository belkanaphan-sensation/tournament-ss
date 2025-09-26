package org.bn.sensation.core.occasion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateOccasionRequestMapper extends BaseDtoMapper<OccasionEntity, UpdateOccasionRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOccasionFromRequest(UpdateOccasionRequest request, @MappingTarget OccasionEntity entity);

}
