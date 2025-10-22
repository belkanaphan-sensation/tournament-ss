package org.bn.sensation.core.criterion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.service.dto.CriterionRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface CriterionRequestMapper extends BaseDtoMapper<CriterionEntity, CriterionRequest> {

    @Override
    CriterionEntity toEntity(CriterionRequest request);

    @Override
    CriterionRequest toDto(CriterionEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCriterionFromRequest(CriterionRequest request, @MappingTarget CriterionEntity criterion);
}
