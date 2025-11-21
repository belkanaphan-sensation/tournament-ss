package org.bn.sensation.core.milestonecriterion.service;

import java.math.BigDecimal;
import java.util.List;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.repository.ActivityUserRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.criterion.repository.CriterionRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.milestonecriterion.repository.MilestoneCriterionRepository;
import org.bn.sensation.core.milestonecriterion.service.dto.CreateMilestoneCriterionRequest;
import org.bn.sensation.core.milestonecriterion.service.dto.MilestoneCriterionDto;
import org.bn.sensation.core.milestonecriterion.service.dto.UpdateMilestoneCriterionRequest;
import org.bn.sensation.core.milestonecriterion.service.mapper.CreateMilestoneCriterionRequestMapper;
import org.bn.sensation.core.milestonecriterion.service.mapper.MilestoneCriterionDtoMapper;
import org.bn.sensation.core.milestonecriterion.service.mapper.UpdateMilestoneCriterionRequestMapper;
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
public class MilestoneCriterionServiceImpl implements MilestoneCriterionService {

    private final MilestoneCriterionRepository milestoneCriterionRepository;
    private final MilestoneCriterionDtoMapper milestoneCriterionDtoMapper;
    private final CreateMilestoneCriterionRequestMapper createMilestoneCriterionRequestMapper;
    private final UpdateMilestoneCriterionRequestMapper updateMilestoneCriterionRequestMapper;
    private final MilestoneRepository milestoneRepository;
    private final CriterionRepository criterionRepository;
    private final ActivityUserRepository activityUserRepository;
    private final MilestoneRuleRepository milestoneRuleRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<MilestoneCriterionEntity> getRepository() {
        return milestoneCriterionRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneCriterionEntity, MilestoneCriterionDto> getMapper() {
        return milestoneCriterionDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneCriterionDto> findAll(Pageable pageable) {
        return milestoneCriterionRepository.findAll(pageable).map(milestoneCriterionDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneCriterionDto create(CreateMilestoneCriterionRequest request) {
        log.info("Создание назначения критерия: правило этапа={}, критерий={}",
                request.getMilestoneRuleId(), request.getCriterionId());

        Preconditions.checkArgument(request.getMilestoneRuleId() != null, "Milestone rule ID не может быть null");
        Preconditions.checkArgument(request.getCriterionId() != null, "Criteria ID не может быть null");

        // Проверяем, что назначение еще не существует
        if (milestoneCriterionRepository.existsByMilestoneRuleIdAndCriterionId(request.getMilestoneRuleId(), request.getCriterionId())) {
            log.warn("Попытка создания дублирующего назначения: правило этапа={}, критерий={}",
                    request.getMilestoneRuleId(), request.getCriterionId());
            throw new IllegalArgumentException("Критерий уже назначен на этот этап");
        }

        // Проверяем существование этапа
        MilestoneRuleEntity milestoneRule = milestoneRuleRepository.getByIdWithCriteriaOrThrow(request.getMilestoneRuleId());

        // Проверяем существование критерия
        CriterionEntity criteria = criterionRepository.getByIdOrThrow(request.getCriterionId());

        log.debug("Найдены правило этапа={} и критерий={} для создания назначения",
                milestoneRule.getId(), criteria.getId());

        if (!milestoneRule.getContestantType().hasPartnerSide()) {
            Preconditions.checkArgument(request.getPartnerSide() == null,
                    "Критерий оценки не может иметь сторону, т.к. правила этапа это не разрешают");
        }
        // Создаем сущность назначения
        MilestoneCriterionEntity assignment = createMilestoneCriterionRequestMapper.toEntity(request);
        assignment.setMilestoneRule(milestoneRule);
        assignment.setCriterion(criteria);
        validateScale(milestoneRule, assignment);

        MilestoneCriterionEntity saved = milestoneCriterionRepository.save(assignment);
        log.info("Назначение критерия успешно создано с id={}", saved.getId());
        return milestoneCriterionDtoMapper.toDto(saved);
    }

    private void validateScale(MilestoneRuleEntity milestoneRule, MilestoneCriterionEntity assignment) {
        log.debug("Валидация шкалы для режима оценки={}, шкала={}, вес={}",
                milestoneRule.getAssessmentMode(), assignment.getScale(), assignment.getWeight());

        switch (milestoneRule.getAssessmentMode()) {
            case PASS -> {
                log.debug("Валидация режима PASS: шкала должна быть 1, количество критериев должно быть 1");
                Preconditions.checkArgument(assignment.getScale().equals(1),
                    "Для режима прохождения (PASS) максимальный балл шкалы оценок равен 1");
                Preconditions.checkArgument(assignment.getId() == null
                        ? milestoneRule.getMilestoneCriteria().size() == 0
                        : milestoneRule.getMilestoneCriteria().size() == 1
                                && milestoneRule.getMilestoneCriteria().iterator().next().getId().equals(assignment.getId()),
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
    public MilestoneCriterionDto update(Long id, UpdateMilestoneCriterionRequest request) {
        Preconditions.checkArgument(id != null, "ID назначения не может быть null");

        MilestoneCriterionEntity assignment = milestoneCriterionRepository.findByIdWithRule(id)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено с id: " + id));

        // Обновляем поля назначения
        if (!assignment.getMilestoneRule().getContestantType().hasPartnerSide()) {
            Preconditions.checkArgument(request.getPartnerSide() == null,
                    "Критерий оценки не может иметь сторону, т.к. правила этапа это не разрешают");
        }
        updateMilestoneCriterionRequestMapper.updateMilestoneCriterionAssignmentFromRequest(request, assignment);
        validateScale(assignment.getMilestoneRule(), assignment);

        MilestoneCriterionEntity saved = milestoneCriterionRepository.save(assignment);
        return milestoneCriterionDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!milestoneCriterionRepository.existsById(id)) {
            throw new IllegalArgumentException("Назначение не найдено с id: " + id);
        }
        milestoneCriterionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneCriterionDto findByMilestoneIdAndCriterionId(Long milestoneId, Long criteriaId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        MilestoneCriterionEntity assignment = milestoneCriterionRepository.findByMilestoneIdAndCriterionId(milestoneId, criteriaId)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено для этапа " + milestoneId + " и критерия " + criteriaId));

        return milestoneCriterionDtoMapper.toDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriterionDto> findByMilestoneId(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");

        return milestoneCriterionRepository.findByMilestoneId(milestoneId)
                .stream()
                .map(milestoneCriterionDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriterionDto> findByCriterionId(Long criteriaId) {
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        return milestoneCriterionRepository.findByCriterionId(criteriaId)
                .stream()
                .map(milestoneCriterionDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneCriterionDto findByMilestoneRuleIdAndCriterionId(Long milestoneRuleId, Long criteriaId) {
        Preconditions.checkArgument(milestoneRuleId != null, "Milestone rule ID не может быть null");
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        MilestoneCriterionEntity assignment = milestoneCriterionRepository.findByMilestoneRuleIdAndCriterionId(milestoneRuleId, criteriaId)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено для правила этапа " + milestoneRuleId + " и критерия " + criteriaId));

        return milestoneCriterionDtoMapper.toDto(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriterionDto> findByMilestoneRuleId(Long milestoneRuleId) {
        Preconditions.checkArgument(milestoneRuleId != null, "Milestone rule ID не может быть null");
        return milestoneCriterionRepository
                .findByMilestoneRuleId(milestoneRuleId).stream()
                .map(milestoneCriterionDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriterionDto> findByMilestoneRuleIdForCurrentUser(Long milestoneRuleId) {
        Preconditions.checkArgument(milestoneRuleId != null, "Milestone ID не может быть null");
        MilestoneRuleEntity milestoneRule = milestoneRuleRepository.getByIdWithMilestoneOrThrow(milestoneRuleId);
        List<MilestoneCriterionEntity> mcae = milestoneCriterionRepository.findByMilestoneRuleId(milestoneRuleId);
        if (mcae.isEmpty()) {
            return List.of();
        }
        ActivityUserEntity uaae = activityUserRepository.getByUserIdAndActivityIdOrThrow(
                        currentUser.getSecurityUser().getId(), milestoneRule.getMilestone().getActivity().getId());

        return mcae.stream()
                .filter(mc -> mc.getPartnerSide() == null || mc.getPartnerSide() == uaae.getPartnerSide())
                .map(milestoneCriterionDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneCriterionDto> findByMilestoneIdForCurrentUser(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(milestoneId);
        List<MilestoneCriterionEntity> mcae = milestoneCriterionRepository.findByMilestoneId(milestoneId);
        if (mcae.isEmpty()) {
            return List.of();
        }
        ActivityUserEntity uaae = activityUserRepository.getByUserIdAndActivityIdOrThrow(currentUser.getSecurityUser().getId(), milestone.getActivity().getId());

        return mcae.stream()
                .filter(mc -> mc.getPartnerSide() == null || mc.getPartnerSide() == uaae.getPartnerSide())
                .map(milestoneCriterionDtoMapper::toDto)
                .toList();
    }
}
