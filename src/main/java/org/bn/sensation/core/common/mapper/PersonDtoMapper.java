package org.bn.sensation.core.common.mapper;

import org.bn.sensation.core.common.dto.PersonDto;
import org.bn.sensation.core.common.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonDtoMapper {

    Person toEntity(PersonDto dto);

    PersonDto toDto(Person entity);
}
