package org.bn.sensation.core.milestoneresult.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestoneresult.entity.MilestoneRoundResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneRoundResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface MilestoneRoundResultDtoMapper extends BaseDtoMapper<MilestoneRoundResultEntity, MilestoneRoundResultDto> {

    @Override
    @Mapping(target = "round", ignore = true)
    MilestoneRoundResultEntity toEntity(MilestoneRoundResultDto dto);

    @Override
    @Mapping(target = "fromExtraRound", source = "round.extraRound")
    @Mapping(target = "roundOrder", source = "round.roundOrder")
    MilestoneRoundResultDto toDto(MilestoneRoundResultEntity entity);
}
