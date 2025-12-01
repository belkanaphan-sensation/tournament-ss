package org.bn.sensation.core.assistant.service.mapper;

import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.assistant.service.dto.AssistantDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class})
public interface AssistantDtoMapper extends BaseDtoMapper<AssistantEntity, AssistantDto> {

    @Override
    AssistantEntity toEntity(AssistantDto dto);

    @Override
    AssistantDto toDto(AssistantEntity entity);
}
