package org.bn.sensation.core.milestoneresult.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class, MilestoneRoundResultDtoMapper.class})
public interface MilestoneResultDtoMapper extends BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> {

    @Override
    MilestoneResultEntity toEntity(MilestoneResultDto dto);

    @Override
    @Mapping(target = "milestoneRoundResults", source = "roundResults")
    MilestoneResultDto toDto(MilestoneResultEntity entity);

}
