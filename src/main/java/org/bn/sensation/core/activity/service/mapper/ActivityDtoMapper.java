package org.bn.sensation.core.activity.service.mapper;

import org.bn.sensation.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.ActivityDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActivityDtoMapper extends BaseDtoMapper<ActivityEntity, ActivityDto> {}
