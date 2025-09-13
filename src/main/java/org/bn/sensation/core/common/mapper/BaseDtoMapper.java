package org.bn.sensation.core.common.mapper;

import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@MapperConfig(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BaseDtoMapper<E extends BaseEntity, D extends EmptyDto> {

    E toEntity(D dto);

    D toDto(E entity);
}
