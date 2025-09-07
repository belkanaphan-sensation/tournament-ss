package org.bn.sensation.core.user.service.mapper;

import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.UpdateUserRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateUserRequestMapper extends BaseDtoMapper<UserEntity, UpdateUserRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget UserEntity entity);

    default OrganizationEntity map(Long organizationId) {
        if (organizationId == null) {
            return null;
        }
        OrganizationEntity organization = new OrganizationEntity();
        organization.setId(organizationId);
        return organization;
    }
}
