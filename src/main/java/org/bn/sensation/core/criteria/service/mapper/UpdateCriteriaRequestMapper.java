package org.bn.sensation.core.criteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.service.dto.UpdateCriteriaRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateCriteriaRequestMapper extends BaseDtoMapper<CriteriaEntity, UpdateCriteriaRequest> {

    @Override
    CriteriaEntity toEntity(UpdateCriteriaRequest request);

    @Override
    UpdateCriteriaRequest toDto(CriteriaEntity entity);

    void updateCriteriaFromRequest(UpdateCriteriaRequest request, @MappingTarget CriteriaEntity criteria);
}
