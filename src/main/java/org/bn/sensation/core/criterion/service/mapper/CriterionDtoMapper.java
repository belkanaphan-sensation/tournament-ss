package org.bn.sensation.core.criterion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.service.dto.CriterionDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class)
public interface CriterionDtoMapper extends BaseDtoMapper<CriterionEntity, CriterionDto> {

    @Override
    CriterionEntity toEntity(CriterionDto dto);

    @Override
    CriterionDto toDto(CriterionEntity entity);
}
