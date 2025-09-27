package org.bn.sensation.core.milestone.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.entity.State;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.AssessmentMode;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

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
        // Используем жадную загрузку для избежания N+1 проблемы
        List<MilestoneEntity> milestones = milestoneRepository.findAllWithCriteriaAssignments();
        List<MilestoneDto> enrichedDtos = milestones.stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();

        // Создаем Page вручную для пагинации
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), enrichedDtos.size());
        List<MilestoneDto> pageContent = enrichedDtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, enrichedDtos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MilestoneDto> findById(Long id) {
        return milestoneRepository.findByIdWithCriteriaAssignments(id)
                .map(this::enrichMilestoneDtoWithStatistics);
    }

    @Override
    @Transactional
    public MilestoneDto create(CreateMilestoneRequest request) {
        Preconditions.checkArgument(request.getActivityId() != null, "ID активности не может быть null");
        ActivityEntity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new EntityNotFoundException("Активность не найдена с id: " + request.getActivityId()));

        MilestoneEntity milestone = createMilestoneRequestMapper.toEntity(request);
        milestone.setActivity(activity);

        if (milestone.getMilestoneOrder() == null) {
            milestone.setMilestoneOrder(calculateNextOrder(request.getActivityId()));
        } else {
            validateOrderSequence(request.getActivityId(), request.getMilestoneOrder(), true);
            reorderMilestones(request.getActivityId(), null, request.getMilestoneOrder());
        }

        MilestoneEntity saved = milestoneRepository.save(milestone);

        addDefaultCriteriaIfNeeded(saved);

        return enrichMilestoneDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public MilestoneDto update(Long id, UpdateMilestoneRequest request) {
        MilestoneEntity milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + id));

        Integer oldOrder = milestone.getMilestoneOrder();

        if (request.getMilestoneOrder() != null) {
            validateOrderSequence(milestone.getActivity().getId(), request.getMilestoneOrder(), false);
            milestone.setMilestoneOrder(request.getMilestoneOrder());

            if (!request.getMilestoneOrder().equals(oldOrder)) {
                reorderMilestones(milestone.getActivity().getId(), id, request.getMilestoneOrder());
            }
        }
        updateMilestoneRequestMapper.updateMilestoneFromRequest(request, milestone);

        MilestoneEntity saved = milestoneRepository.save(milestone);
        return enrichMilestoneDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!milestoneRepository.existsById(id)) {
            throw new IllegalArgumentException("Этап не найден с id: " + id);
        }
        milestoneRepository.deleteById(id);
    }


    private void addDefaultCriteriaIfNeeded(MilestoneEntity milestone) {
        if (milestone.getCriteriaAssignments().isEmpty()) {
            // Получаем критерий по умолчанию по имени "Прохождение"
            CriteriaEntity defaultCriteria = criteriaRepository.findByName(DEFAULT_CRITERIA)
                    .orElseThrow(() -> new EntityNotFoundException("Критерий по умолчанию 'Прохождение' не найден"));

            MilestoneCriteriaAssignmentEntity assignment = MilestoneCriteriaAssignmentEntity.builder()
                    .milestone(milestone)
                    .criteria(defaultCriteria)
                    .scale(1)
                    .build();

            milestoneCriteriaAssignmentRepository.save(assignment);
            
            milestone.getCriteriaAssignments().add(assignment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDto> findByActivityId(Long id) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        return milestoneRepository.findByActivityIdWithCriteriaAssignments(id).stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDto> findByActivityIdInLifeStates(Long id) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        return milestoneRepository.findByActivityIdAndStateInWithCriteriaAssignments(id, State.LIFE_STATES).stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();
    }

    /**
     * Обогащает MilestoneDto статистикой по раундам и AssessmentMode
     */
    private MilestoneDto enrichMilestoneDtoWithStatistics(MilestoneEntity milestone) {
        MilestoneDto dto = milestoneDtoMapper.toDto(milestone);
        dto.setAssessmentMode(calculateAssessmentMode(milestone));
        dto.setCompletedRoundsCount((int) milestone.getRounds().stream()
                .filter(round -> round.getState() == State.COMPLETED)
                .count());
        dto.setTotalRoundsCount(milestone.getRounds().size());

        return dto;
    }

    /**
     * Рассчитывает AssessmentMode на основе criteriaAssignments
     * Если у milestone всего 1 criteriaAssignments и у него scale = 1, то PASS, иначе CRITERIA
     */
    private AssessmentMode calculateAssessmentMode(MilestoneEntity milestone) {
        if (milestone.getCriteriaAssignments() == null || milestone.getCriteriaAssignments().isEmpty()) {
            return AssessmentMode.CRITERIA; // По умолчанию CRITERIA если еще нет назначений
        }
        if (milestone.getCriteriaAssignments().size() == 1
                && milestone.getCriteriaAssignments().iterator().next().getScale() == 1) {
            return AssessmentMode.PASS;

        }
        return AssessmentMode.CRITERIA;
    }

    /**
     * Валидирует, что порядок последовательный (нельзя проставить порядок 5 если не существует этапов с порядками меньше)
     */
    private void validateOrderSequence(Long activityId, Integer newOrder, boolean create) {
        Integer maxOrder = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(activityId).stream()
                .map(MilestoneEntity::getMilestoneOrder)
                .max(Integer::compareTo)
                .orElse(-1);

        int maxNewOrder = create ? maxOrder + 1 : maxOrder;
        if (newOrder > maxNewOrder) {
            throw new IllegalArgumentException("Нельзя установить порядок " + newOrder +
                    ". Максимальный существующий порядок: " + maxOrder +
                    ". Можно установить порядок от 0 до " + maxNewOrder);
        }
    }

    /**
     * Пересчитывает порядок всех этапов активности при изменении порядка одного этапа
     */
    private void reorderMilestones(Long activityId, Long currentMilestoneId, Integer newOrder) {
        List<MilestoneEntity> milestones = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(activityId)
                .stream()
                .filter(m -> currentMilestoneId == null || !m.getId().equals(currentMilestoneId)) // Исключаем текущий этап (если указан)
                .collect(Collectors.toList());

        for (int i = 0; i < milestones.size(); i++) {
            MilestoneEntity milestone = milestones.get(i);
            if (i >= newOrder) {
                milestone.setMilestoneOrder(i + 1);
            } else {
                milestone.setMilestoneOrder(i);
            }
        }
        milestoneRepository.saveAll(milestones);
    }

    /**
     * Рассчитывает следующий порядковый номер для этапа в рамках активности
     */
    private Integer calculateNextOrder(Long activityId) {
        return milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(activityId)
                .stream()
                .map(MilestoneEntity::getMilestoneOrder)
                .max(Integer::compareTo)
                .map(max -> max + 1)
                .orElse(0);
    }
}
