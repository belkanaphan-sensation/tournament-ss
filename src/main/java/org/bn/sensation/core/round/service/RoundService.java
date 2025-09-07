package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoundService extends BaseService<RoundEntity, RoundDto> {

    // CRUD operations
    Page<RoundDto> findAll(Pageable pageable);

    RoundDto create(CreateRoundRequest request);

    RoundDto update(Long id, UpdateRoundRequest request);

    void deleteById(Long id);
}
