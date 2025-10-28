package org.bn.sensation.core.organization.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.organization.entity.OrganizationEntity;

import jakarta.persistence.EntityNotFoundException;

public interface OrganizationRepository extends BaseRepository<OrganizationEntity> {

    Optional<OrganizationEntity> findByName(String name);

    Optional<OrganizationEntity> findByEmail(String email);

    default OrganizationEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Организация не найдена: " + id));
    }
}
