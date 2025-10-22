package org.bn.sensation.core.milestonecriterion.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.service.dto.MilestoneCriterionDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface MilestoneCriterionDtoMapper extends BaseDtoMapper<MilestoneCriterionEntity, MilestoneCriterionDto> {

    @Override
    MilestoneCriterionEntity toEntity(MilestoneCriterionDto dto);

    @Override
    MilestoneCriterionDto toDto(MilestoneCriterionEntity entity);
}
