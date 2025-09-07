package org.bn.sensation.core.occasion.service.mapper;

import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.service.dto.CreateOccasionRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateOccasionRequestMapper extends BaseDtoMapper<OccasionEntity, CreateOccasionRequest> {
    @Override
    @Mapping(target = "organization", source = "organizationId")
    OccasionEntity toEntity(CreateOccasionRequest dto);

    @Override
    @Mapping(target = "organizationId", source = "organization.id")
    CreateOccasionRequest toDto(OccasionEntity entity);

    default OrganizationEntity map(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        return organization;
    }
}
