package org.bn.sensation.core.common.mapper;

import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressDtoMapper {

    Address toEntity(AddressDto dto);

    AddressDto toDto(Address entity);
}
