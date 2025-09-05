package org.bn.sensation.core.common.service;

import java.util.Optional;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

public interface BaseService<T extends BaseEntity, R extends BaseDto> {

    BaseRepository<T> getRepository();

    BaseDtoMapper<T, R> getMapper();

    @Transactional
    default R save(T entity) {
        return getMapper().toDto(getRepository().save(entity));
    }

    @Transactional
    default void delete(T entity) {
        getRepository().delete(entity);
    }

    @Transactional(readOnly = true)
    default Optional<R> findById(Long id) {
        return getRepository().findById(id)
                .map(getMapper()::toDto);
    }
}
