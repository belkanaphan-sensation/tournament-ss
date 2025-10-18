package org.bn.sensation.core.milestonecriteria.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestonecriteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestonecriteria.service.dto.MilestoneCriteriaAssignmentDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface MilestoneCriteriaAssignmentDtoMapper extends BaseDtoMapper<MilestoneCriteriaAssignmentEntity, MilestoneCriteriaAssignmentDto> {

    @Override
    MilestoneCriteriaAssignmentEntity toEntity(MilestoneCriteriaAssignmentDto dto);

    @Override
    MilestoneCriteriaAssignmentDto toDto(MilestoneCriteriaAssignmentEntity entity);
}
