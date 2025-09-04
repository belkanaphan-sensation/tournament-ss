package org.bn.sensation.core.common.service;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
public abstract class BaseService<T extends BaseEntity, R extends BaseDto> {

  protected final BaseRepository<T> baseRepository;
  protected final BaseDtoMapper<T, R> baseDtoMapper;

  public R save(T entity) {
    return baseDtoMapper.toDto(baseRepository.save(entity));
  }

  public void delete(T entity) {
    baseRepository.delete(entity);
  }

  public R findById(Long id) {
    return baseDtoMapper.toDto(baseRepository.findById(id).orElse(null));
  }
}
