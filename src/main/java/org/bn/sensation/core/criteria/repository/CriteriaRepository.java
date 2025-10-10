package org.bn.sensation.core.criteria.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaRepository extends BaseRepository<CriteriaEntity> {

    Optional<CriteriaEntity> findByName(String name);
}
