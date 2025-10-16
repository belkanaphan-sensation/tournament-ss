package org.bn.sensation.core.criteria.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.service.dto.CriteriaRequest;
import org.bn.sensation.core.criteria.service.dto.CriteriaDto;
import org.bn.sensation.core.criteria.service.mapper.CriteriaDtoMapper;
import org.bn.sensation.core.criteria.service.mapper.CriteriaRequestMapper;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {

    private final CriteriaRepository criteriaRepository;
    private final CriteriaDtoMapper criteriaDtoMapper;
    private final CriteriaRequestMapper criteriaRequestMapper;
    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Override
    public BaseRepository<CriteriaEntity> getRepository() {
        return criteriaRepository;
    }

    @Override
    public BaseDtoMapper<CriteriaEntity, CriteriaDto> getMapper() {
        return criteriaDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CriteriaDto> findAll(Pageable pageable) {
        return criteriaRepository.findAll(pageable).map(criteriaDtoMapper::toDto);
    }

    @Override
    @Transactional
    public CriteriaDto create(CriteriaRequest request) {
        log.info("Создание критерия: название={}", request.getName());
        
        Preconditions.checkArgument(StringUtils.hasText(request.getName()), "Название критерия не может быть пустым");

        // Проверяем уникальность названия
        if (criteriaRepository.findByName(request.getName()).isPresent()) {
            log.warn("Попытка создания критерия с существующим названием: {}", request.getName());
            throw new IllegalArgumentException("Критерий с названием '" + request.getName() + "' уже существует");
        }

        CriteriaEntity criteria = criteriaRequestMapper.toEntity(request);
        CriteriaEntity saved = criteriaRepository.save(criteria);
        log.info("Критерий успешно создан с id={}, название={}", saved.getId(), saved.getName());
        return criteriaDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CriteriaDto update(Long id, CriteriaRequest request) {
        CriteriaEntity criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Критерий не найден с id: " + id));

        // Валидация названия если оно обновляется
        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Название критерия не может быть пустым");
            }

            // Проверяем уникальность названия (исключая текущий критерий)
            criteriaRepository.findByName(request.getName())
                    .ifPresent(existingCriteria -> {
                        if (!existingCriteria.getId().equals(id)) {
                            throw new IllegalArgumentException("Критерий с названием '" + request.getName() + "' уже существует");
                        }
                    });
        } else {
            // Если название null, это тоже ошибка
            throw new IllegalArgumentException("Название критерия не может быть пустым");
        }

        // Обновляем поля критерия
        criteriaRequestMapper.updateCriteriaFromRequest(request, criteria);

        CriteriaEntity saved = criteriaRepository.save(criteria);
        return criteriaDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление критерия с id={}", id);
        
        CriteriaEntity criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Критерий не найден с id: " + id));

        log.debug("Найден критерий={} для удаления", criteria.getName());

        // Проверяем, используется ли критерий в этапах
        if (milestoneCriteriaAssignmentRepository.existsByCriteriaId(id)) {
            log.warn("Нельзя удалить критерий={} - он используется в этапах", criteria.getName());
            throw new IllegalArgumentException("Нельзя удалить критерий, который используется в этапах");
        }

        criteriaRepository.deleteById(id);
        log.info("Критерий={} успешно удален", criteria.getName());
    }

}
