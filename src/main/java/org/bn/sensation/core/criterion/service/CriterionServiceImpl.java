package org.bn.sensation.core.criterion.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.criterion.service.dto.CriterionRequest;
import org.bn.sensation.core.criterion.service.dto.CriterionDto;
import org.bn.sensation.core.criterion.service.mapper.CriterionDtoMapper;
import org.bn.sensation.core.criterion.service.mapper.CriterionRequestMapper;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriterionServiceImpl implements CriterionService {

    private final CriterionRepository criterionRepository;
    private final CriterionDtoMapper criterionDtoMapper;
    private final CriterionRequestMapper criterionRequestMapper;
    private final MilestoneCriterionRepository milestoneCriterionRepository;

    @Override
    public BaseRepository<CriterionEntity> getRepository() {
        return criterionRepository;
    }

    @Override
    public BaseDtoMapper<CriterionEntity, CriterionDto> getMapper() {
        return criterionDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CriterionDto> findAll(Pageable pageable) {
        return criterionRepository.findAll(pageable).map(criterionDtoMapper::toDto);
    }

    @Override
    @Transactional
    public CriterionDto create(CriterionRequest request) {
        log.info("Создание критерия: название={}", request.getName());

        Preconditions.checkArgument(StringUtils.hasText(request.getName()), "Название критерия не может быть пустым");

        // Проверяем уникальность названия
        if (criterionRepository.findByName(request.getName()).isPresent()) {
            log.warn("Попытка создания критерия с существующим названием: {}", request.getName());
            throw new IllegalArgumentException("Критерий с названием '" + request.getName() + "' уже существует");
        }

        CriterionEntity criterion = criterionRequestMapper.toEntity(request);
        CriterionEntity saved = criterionRepository.save(criterion);
        log.info("Критерий успешно создан с id={}, название={}", saved.getId(), saved.getName());
        return criterionDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CriterionDto update(Long id, CriterionRequest request) {
        CriterionEntity criterion = criterionRepository.getByIdOrThrow(id);

        // Валидация названия если оно обновляется
        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Название критерия не может быть пустым");
            }

            // Проверяем уникальность названия (исключая текущий критерий)
            criterionRepository.findByName(request.getName())
                    .ifPresent(existingCriterion -> {
                        if (!existingCriterion.getId().equals(id)) {
                            throw new IllegalArgumentException("Критерий с названием '" + request.getName() + "' уже существует");
                        }
                    });
        } else {
            // Если название null, это тоже ошибка
            throw new IllegalArgumentException("Название критерия не может быть пустым");
        }

        // Обновляем поля критерия
        criterionRequestMapper.updateCriterionFromRequest(request, criterion);

        CriterionEntity saved = criterionRepository.save(criterion);
        return criterionDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление критерия с id={}", id);

        CriterionEntity criterion = criterionRepository.getByIdOrThrow(id);

        log.debug("Найден критерий={} для удаления", criterion.getName());

        // Проверяем, используется ли критерий в этапах
        if (milestoneCriterionRepository.existsByCriterionId(id)) {
            log.warn("Нельзя удалить критерий={} - он используется в этапах", criterion.getName());
            throw new IllegalArgumentException("Нельзя удалить критерий, который используется в этапах");
        }

        criterionRepository.deleteById(id);
        log.info("Критерий={} успешно удален", criterion.getName());
    }

}
