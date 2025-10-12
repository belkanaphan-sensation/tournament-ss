package org.bn.sensation.core.occasion.service.mapper;

import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateOccasionRequestMapper extends BaseDtoMapper<OccasionEntity, CreateOccasionRequest> {
    @Override
    @Mapping(target = "organization", ignore = true)
    OccasionEntity toEntity(CreateOccasionRequest dto);


}
