package org.bn.sensation.core.activity.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.service.dto.UpdateActivityRequest;
import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateActivityRequestMapper  extends BaseDtoMapper<ActivityEntity, UpdateActivityRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateActivityFromRequest(UpdateActivityRequest request, @MappingTarget ActivityEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromRequest(AddressDto request, @MappingTarget Address address);

}
