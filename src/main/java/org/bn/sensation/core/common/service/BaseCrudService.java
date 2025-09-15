package org.bn.sensation.core.common.service;

import org.bn.sensation.core.common.dto.BaseDto;
import org.bn.sensation.core.common.dto.EmptyDto;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Базовый интерфейс для CRUD операций с пагинацией.
 * Содержит стандартные методы для работы с сущностями.
 *
 * @param <T> тип сущности
 * @param <R> тип DTO
 * @param <C> тип CreateRequest
 * @param <U> тип UpdateRequest
 */
public interface BaseCrudService<T extends BaseEntity, R extends BaseDto, C extends EmptyDto, U extends EmptyDto>
        extends BaseService<T, R> {

    /**
     * Получить все сущности с пагинацией
     *
     * @param pageable параметры пагинации
     * @return страница с сущностями
     */
    Page<R> findAll(Pageable pageable);

    /**
     * Создать новую сущность
     *
     * @param request данные для создания
     * @return созданная сущность
     */
    R create(C request);

    /**
     * Обновить существующую сущность
     *
     * @param id идентификатор сущности
     * @param request данные для обновления
     * @return обновленная сущность
     */
    R update(Long id, U request);

    /**
     * Удалить сущность по идентификатору
     *
     * @param id идентификатор сущности
     */
    void deleteById(Long id);
}
