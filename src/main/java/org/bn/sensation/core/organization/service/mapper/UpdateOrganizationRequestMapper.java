package org.bn.sensation.core.organization.service.mapper;

import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.organization.service.dto.UpdateOrganizationRequest;
import org.bn.sensation.core.common.dto.AddressDto;
import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateOrganizationRequestMapper extends BaseDtoMapper<OrganizationEntity, UpdateOrganizationRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrganizationFromRequest(UpdateOrganizationRequest request, @MappingTarget OrganizationEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromRequest(AddressDto request, @MappingTarget Address address);
}
