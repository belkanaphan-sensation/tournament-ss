package org.bn.sensation.core.judgemilstoneresult.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.judgemilstoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultDto;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface JudgeMilestoneResultDtoMapper extends BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultDto> {

    @Override
    JudgeMilestoneResultEntity toEntity(JudgeMilestoneResultDto dto);

    @Override
    JudgeMilestoneResultDto toDto(JudgeMilestoneResultEntity entity);
}
