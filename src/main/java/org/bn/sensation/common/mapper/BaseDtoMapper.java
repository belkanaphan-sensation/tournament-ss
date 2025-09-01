package org.bn.sensation.common.mapper;

import org.bn.sensation.common.dto.BaseDto;
import org.bn.sensation.common.entity.BaseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BaseDtoMapper<E extends BaseEntity, D extends BaseDto> {

    E toEntity(D dto);

    D toDto(E entity);
}
