package org.bn.sensation.core.criteria.repository;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CriteriaRepository extends BaseRepository<CriteriaEntity> {

    /**
     * Найти критерий по умолчанию по имени "Прохождение"
     */
    Optional<CriteriaEntity> findByName(String name);
}
