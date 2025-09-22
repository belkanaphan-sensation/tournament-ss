package org.bn.sensation.core.milestone.service;

import com.google.common.base.Preconditions;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.milestone.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.MilestoneDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    //todo как то откуда-то брать?
    private static final String DEFAULT_CRITERIA = "Прохождение";

    private final MilestoneRepository milestoneRepository;
    private final MilestoneDtoMapper milestoneDtoMapper;
    private final CreateMilestoneRequestMapper createMilestoneRequestMapper;
    private final UpdateMilestoneRequestMapper updateMilestoneRequestMapper;
    private final ActivityRepository activityRepository;
    private final CriteriaRepository criteriaRepository;
    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;

    @Override
    public BaseRepository<MilestoneEntity> getRepository() {
        return milestoneRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneEntity, MilestoneDto> getMapper() {
        return milestoneDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneDto> findAll(Pageable pageable) {
        return milestoneRepository.findAll(pageable).map(milestoneDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneDto create(CreateMilestoneRequest request) {
        // Проверяем, что активность существует
        ActivityEntity activity = findActivityById(request.getActivityId());

        // Создаем сущность вехи
        MilestoneEntity milestone = createMilestoneRequestMapper.toEntity(request);
        milestone.setActivity(activity);

        MilestoneEntity saved = milestoneRepository.save(milestone);

        // Добавляем критерий по умолчанию, если критерии не указаны
        addDefaultCriteriaIfNeeded(saved);

        return milestoneDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneDto update(Long id, UpdateMilestoneRequest request) {
        MilestoneEntity milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + id));

        // Обновляем поля вехи
        updateMilestoneRequestMapper.updateMilestoneFromRequest(request, milestone);

        // Обновляем активность
        if (request.getActivityId() != null) {
            ActivityEntity activity = findActivityById(request.getActivityId());
            milestone.setActivity(activity);
        }

        MilestoneEntity saved = milestoneRepository.save(milestone);
        return milestoneDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!milestoneRepository.existsById(id)) {
            throw new IllegalArgumentException("Этап не найден с id: " + id);
        }
        milestoneRepository.deleteById(id);
    }

    private ActivityEntity findActivityById(Long activityId) {
        if (activityId == null) {
            return null;
        }
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Активность не найдена с id: " + activityId));
    }

    private void addDefaultCriteriaIfNeeded(MilestoneEntity milestone) {
        // Проверяем, есть ли уже критерии у этапа
        if (milestone.getCriteriaAssignments().isEmpty()) {
            // Получаем критерий по умолчанию по имени "Прохождение"
            CriteriaEntity defaultCriteria = criteriaRepository.findByName(DEFAULT_CRITERIA)
                    .orElseThrow(() -> new EntityNotFoundException("Критерий по умолчанию 'Прохождение' не найден"));

            // Создаем связь с критерием по умолчанию
            MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                    .milestone(milestone)
                    .criteria(defaultCriteria)
                    .competitionRole(null) // Критерий по умолчанию не привязан к роли
                    .build();

            milestoneCriteriaAssignmentRepository.save(assignment);
        }
    }

    @Override
    public Page<MilestoneDto> findByActivityId(Long id, Pageable pageable) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        return milestoneRepository.findByActivityId(id, pageable).map(milestoneDtoMapper::toDto);
    }
}
