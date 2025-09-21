package org.bn.sensation.core.criteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.service.dto.CriteriaDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface CriteriaDtoMapper extends BaseDtoMapper<CriteriaEntity, CriteriaDto> {

    @Override
    CriteriaEntity toEntity(CriteriaDto dto);

    @Override
    CriteriaDto toDto(CriteriaEntity entity);
}
