package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.repository.RoundResultRepository;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.bn.sensation.core.round.service.mapper.RoundResultDtoMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoundResultServiceImpl implements RoundResultService {

    private final RoundResultRepository roundResultRepository;
    private final RoundResultDtoMapper roundResultDtoMapper;

    @Override
    public BaseRepository<RoundResultEntity> getRepository() {
        return roundResultRepository;
    }

    @Override
    public BaseDtoMapper<RoundResultEntity, RoundResultDto> getMapper() {
        return roundResultDtoMapper;
    }

}
