package org.bn.sensation.core.criteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.service.dto.CreateCriteriaRequest;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface CreateCriteriaRequestMapper extends BaseDtoMapper<CriteriaEntity, CreateCriteriaRequest> {

    @Override
    CriteriaEntity toEntity(CreateCriteriaRequest request);

    @Override
    CreateCriteriaRequest toDto(CriteriaEntity entity);
}
