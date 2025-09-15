package org.bn.sensation.core.occasion.service.mapper;

import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.UpdateOccasionRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateOccasionRequestMapper extends BaseDtoMapper<OccasionEntity, UpdateOccasionRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOccasionFromRequest(UpdateOccasionRequest request, @MappingTarget OccasionEntity entity);

    default OrganizationEntity map(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        return organization;
    }
}
