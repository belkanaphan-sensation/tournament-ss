package org.bn.sensation.common.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bn.sensation.common.dto.BaseDto;
import org.bn.sensation.common.entity.BaseEntity;
import org.bn.sensation.common.mapper.BaseDtoMapper;
import org.bn.sensation.common.repository.BaseRepository;

@Slf4j
@Transactional
public abstract class BaseService<T extends BaseEntity, R extends BaseDto> {

    protected final BaseRepository<T> baseRepository;
    protected final BaseDtoMapper<T, R> baseDtoMapper;

    protected BaseService(BaseRepository<T> baseRepository, BaseDtoMapper<T, R> baseDtoMapper) {
        this.baseRepository = baseRepository;
        this.baseDtoMapper = baseDtoMapper;
    }
}
