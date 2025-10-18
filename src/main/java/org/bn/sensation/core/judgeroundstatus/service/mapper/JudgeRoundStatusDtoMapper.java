package org.bn.sensation.core.judgeroundstatus.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.service.dto.JudgeRoundStatusDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface JudgeRoundStatusDtoMapper extends BaseDtoMapper<JudgeRoundStatusEntity, JudgeRoundStatusDto> {

    JudgeRoundStatusDto toDto(JudgeRoundStatusEntity extraRound);

    @Mapping(target = "judge", ignore = true)
    @Mapping(target = "round", ignore = true)
    JudgeRoundStatusEntity toEntity(JudgeRoundStatusDto request);
}
