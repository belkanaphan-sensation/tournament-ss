package org.bn.sensation.core.milestonecriteria.service;

import java.math.BigDecimal;
import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.milestonecriteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.milestonecriteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestonecriteria.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.milestonecriteria.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.milestonecriteria.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.milestonecriteria.service.mapper.CreateMilestoneCriteriaAssignmentRequestMapper;
import org.bn.sensation.core.milestonecriteria.service.mapper.MilestoneCriteriaAssignmentDtoMapper;
import org.bn.sensation.core.milestonecriteria.service.mapper.UpdateMilestoneCriteriaAssignmentRequestMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.useractivity.repository.UserActivityAssignmentRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneCriteriaAssignmentServiceImpl implements MilestoneCriteriaAssignmentService {

    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;
    private final MilestoneCriteriaAssignmentDtoMapper milestoneCriteriaAssignmentDtoMapper;
    private final CreateMilestoneCriteriaAssignmentRequestMapper createMilestoneCriteriaAssignmentRequestMapper;
    private final UpdateMilestoneCriteriaAssignmentRequestMapper updateMilestoneCriteriaAssignmentRequestMapper;
    private final MilestoneRepository milestoneRepository;
    private final CriteriaRepository criteriaRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final MilestoneRuleRepository milestoneRuleRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<MilestoneCriteriaAssignmentEntity> getRepository() {
        return milestoneCriteriaAssignmentRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneCriteriaAssignmentEntity, MilestoneCriteriaAssignmentDto> getMapper() {
        return milestoneCriteriaAssignmentDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneCriteriaAssignmentDto> findAll(Pageable pageable) {
        return milestoneCriteriaAssignmentRepository.findAll(pageable).map(milestoneCriteriaAssignmentDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneCriteriaAssignmentDto create(CreateMilestoneCriteriaAssignmentRequest request) {
        log.info("Создание назначения критерия: правило этапа={}, критерий={}",
                request.getMilestoneRuleId(), request.getCriteriaId());

        Preconditions.checkArgument(request.getMilestoneRuleId() != null, "Milestone rule ID не может быть null");
        Preconditions.checkArgument(request.getCriteriaId() != null, "Criteria ID не может быть null");

        // Проверяем, что назначение еще не существует
        if (milestoneCriteriaAssignmentRepository.existsByMilestoneRuleIdAndCriteriaId(request.getMilestoneRuleId(), request.getCriteriaId())) {
            log.warn("Попытка создания дублирующего назначения: правило этапа={}, критерий={}",
                    request.getMilestoneRuleId(), request.getCriteriaId());
            throw new IllegalArgumentException("Критерий уже назначен на этот этап");
        }

        // Проверяем существование этапа
        MilestoneRuleEntity milestoneRule = milestoneRuleRepository.findByIdWithCriteria(request.getMilestoneRuleId())
                .orElseThrow(() -> new EntityNotFoundException("Правило этапа не найден с id: " + request.getMilestoneRuleId()));

        // Проверяем существование критерия
        CriteriaEntity criteria = criteriaRepository.findById(request.getCriteriaId())
                .orElseThrow(() -> new EntityNotFoundException("Критерий не найден с id: " + request.getCriteriaId()));

        log.debug("Найдены правило этапа={} и критерий={} для создания назначения",
                milestoneRule.getId(), criteria.getId());

        // Создаем сущность назначения
        MilestoneCriteriaAssignmentEntity assignment = createMilestoneCriteriaAssignmentRequestMapper.toEntity(request);
        assignment.setMilestoneRule(milestoneRule);
        assignment.setCriteria(criteria);
        validateScale(milestoneRule, assignment);

        MilestoneCriteriaAssignmentEntity saved = milestoneCriteriaAssignmentRepository.save(assignment);
        log.info("Назначение критерия успешно создано с id={}", saved.getId());
        return milestoneCriteriaAssignmentDtoMapper.toDto(saved);
    }

    private void validateScale(MilestoneRuleEntity milestoneRule, MilestoneCriteriaAssignmentEntity assignment) {
        log.debug("Валидация шкалы для режима оценки={}, шкала={}, вес={}",
                milestoneRule.getAssessmentMode(), assignment.getScale(), assignment.getWeight());

        switch (milestoneRule.getAssessmentMode()) {
            case PASS -> {
                log.debug("Валидация режима PASS: шкала должна быть 1, количество критериев должно быть 1");
                Preconditions.checkArgument(assignment.getScale().equals(1),
                    "Для режима прохождения (PASS) максимальный балл шкалы оценок равен 1");
                Preconditions.checkArgument(assignment.getId() == null
                        ? milestoneRule.getCriteriaAssignments().size() == 0
                        : milestoneRule.getCriteriaAssignments().size() == 1
                                && milestoneRule.getCriteriaAssignments().iterator().next().getId().equals(assignment.getId()),
                        "Для режима прохождения (PASS) количество критериев должно быть 1");
                log.debug("Валидация режима PASS прошла успешно");
            }
            case SCORE -> {
                log.debug("Валидация режима SCORE: шкала должна быть больше 1");
                Preconditions.checkArgument(assignment.getScale() != null && assignment.getScale().compareTo(1) > 0,
                        "Для режима шкалы оценок (SCORE) шкала должна быть установлена и быть больше 1");
                log.debug("Валидация режима SCORE прошла успешно");
            }
            case PLACE -> {
                log.debug("Валидация режима PLACE: вес должен быть равен 1");
                Preconditions.checkArgument(assignment.getWeight().equals(BigDecimal.ONE),
                        "Для режима распределения по местам коэффициент критерия должен быть равен 1");
                log.debug("Валидация режима PLACE прошла успешно");
            }
        }
    }

    @Override
    @Transactional
    public MilestoneCriteriaAssignmentDto update(Long id, UpdateMilestoneCriteriaAssignmentRequest request) {
        Preconditions.checkArgument(id != null, "ID назначения не может быть null");

        MilestoneCriteriaAssignmentEntity assignment = milestoneCriteriaAssignmentRepository.findByIdWithRule(id)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено с id: " + id));

        // Обновляем поля назначения
        updateMilestoneCriteriaAssignmentRequestMapper.updateMilestoneCriteriaAssignmentFromRequest(request, assignment);
        validateScale(assignment.getMilestoneRule(), assignment);

        MilestoneCriteriaAssignmentEntity saved = milestoneCriteriaAssignmentRepository.save(assignment);
        return milestoneCriteriaAssignmentDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!milestoneCriteriaAssignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Назначение не найдено с id: " + id);
        }
        milestoneCriteriaAssignmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneCriteriaAssignmentDto findByMilestoneIdAndCriteriaId(Long milestoneId, Long criteriaId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        MilestoneCriteriaAssignmentEntity assignment = milestoneCriteriaAssignmentRepository.findByMilestoneIdAndCriteriaId(milestoneId, criteriaId)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено для этапа " + milestoneId + " и критерия " + criteriaId));

        return milestoneCriteriaAssignmentDtoMapper.toDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriteriaAssignmentDto> findByMilestoneId(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");

        return milestoneCriteriaAssignmentRepository.findByMilestoneId(milestoneId)
                .stream()
                .map(milestoneCriteriaAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriteriaAssignmentDto> findByCriteriaId(Long criteriaId) {
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        return milestoneCriteriaAssignmentRepository.findByCriteriaId(criteriaId)
                .stream()
                .map(milestoneCriteriaAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneCriteriaAssignmentDto findByMilestoneRuleIdAndCriteriaId(Long milestoneRuleId, Long criteriaId) {
        Preconditions.checkArgument(milestoneRuleId != null, "Milestone rule ID не может быть null");
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        MilestoneCriteriaAssignmentEntity assignment = milestoneCriteriaAssignmentRepository.findByMilestoneRuleIdAndCriteriaId(milestoneRuleId, criteriaId)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено для правила этапа " + milestoneRuleId + " и критерия " + criteriaId));

        return milestoneCriteriaAssignmentDtoMapper.toDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriteriaAssignmentDto> findByMilestoneRuleId(Long milestoneRuleId) {
        Preconditions.checkArgument(milestoneRuleId != null, "Milestone rule ID не может быть null");
        return milestoneCriteriaAssignmentRepository
                .findByMilestoneRuleId(milestoneRuleId).stream()
                .map(milestoneCriteriaAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriteriaAssignmentDto> findByMilestoneRuleIdForCurrentUser(Long milestoneRuleId) {
        Preconditions.checkArgument(milestoneRuleId != null, "Milestone ID не может быть null");
        MilestoneRuleEntity milestoneRule = milestoneRuleRepository.findByIdWithMilestone(milestoneRuleId)
                .orElseThrow(() -> new EntityNotFoundException("Правило этапа не найден с id: " + milestoneRuleId));
        List<MilestoneCriteriaAssignmentEntity> mcae = milestoneCriteriaAssignmentRepository.findByMilestoneRuleId(milestoneRuleId);
        if (mcae.isEmpty()) {
            return List.of();
        }
        UserActivityAssignmentEntity uaae = userActivityAssignmentRepository.findByUserIdAndActivityId(
                        currentUser.getSecurityUser().getId(), milestoneRule.getMilestone().getActivity().getId())
                .orElseThrow(() -> new EntityNotFoundException("Данный юзер не подписан на активность, включающую этап правилом с id: " + milestoneRuleId));

        return mcae.stream()
                .filter(mc -> mc.getPartnerSide() == null || mc.getPartnerSide() == uaae.getPartnerSide())
                .map(milestoneCriteriaAssignmentDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriteriaAssignmentDto> findByMilestoneIdForCurrentUser(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + milestoneId));
        List<MilestoneCriteriaAssignmentEntity> mcae = milestoneCriteriaAssignmentRepository.findByMilestoneId(milestoneId);
        if (mcae.isEmpty()) {
            return List.of();
        }
        UserActivityAssignmentEntity uaae = userActivityAssignmentRepository.findByUserIdAndActivityId(currentUser.getSecurityUser().getId(), milestone.getActivity().getId())
                .orElseThrow(() -> new EntityNotFoundException("Данный юзер не подписан на активность, включающую этап с id: " + milestoneId));

        return mcae.stream()
                .filter(mc -> mc.getPartnerSide() == null || mc.getPartnerSide() == uaae.getPartnerSide())
                .map(milestoneCriteriaAssignmentDtoMapper::toDto)
                .toList();
    }
}
