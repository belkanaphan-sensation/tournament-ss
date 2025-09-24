package org.bn.sensation.core.criteria.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneCriteriaAssignmentRepository extends BaseRepository<MilestoneCriteriaAssignmentEntity> {

    Optional<MilestoneCriteriaAssignmentEntity> findByMilestoneIdAndCriteriaId(Long milestoneId, Long criteriaId);

    Page<MilestoneCriteriaAssignmentEntity> findByMilestoneId(Long milestoneId, Pageable pageable);

    Page<MilestoneCriteriaAssignmentEntity> findByCriteriaId(Long criteriaId, Pageable pageable);

    boolean existsByMilestoneIdAndCriteriaId(Long milestoneId, Long criteriaId);

    boolean existsByCriteriaId(Long criteriaId);

    long countByMilestoneId(Long milestoneId);
}
