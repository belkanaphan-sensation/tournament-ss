package org.bn.sensation.core.criteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.service.dto.CriteriaRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface CriteriaRequestMapper extends BaseDtoMapper<CriteriaEntity, CriteriaRequest> {

    @Override
    CriteriaEntity toEntity(CriteriaRequest request);

    @Override
    CriteriaRequest toDto(CriteriaEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCriteriaFromRequest(CriteriaRequest request, @MappingTarget CriteriaEntity criteria);
}
