package org.bn.sensation.core.organization.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;

public interface OrganizationRepository extends BaseRepository<OrganizationEntity> {

    Optional<OrganizationEntity> findByName(String name);

    Optional<OrganizationEntity> findByEmail(String email);
}
