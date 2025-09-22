package org.bn.sensation.core.round.service;

import jakarta.validation.constraints.NotNull;
import org.bn.sensation.core.common.service.BaseCrudService;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoundService extends BaseCrudService<
        RoundEntity,
        RoundDto,
        CreateRoundRequest,
        UpdateRoundRequest> {

    Page<RoundDto> findByMilestoneId(@NotNull Long id, Pageable pageable);
}
