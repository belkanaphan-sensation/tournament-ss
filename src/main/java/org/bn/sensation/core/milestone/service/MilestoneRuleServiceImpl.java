package org.bn.sensation.core.milestone.service;


import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRuleRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRuleRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneRuleDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRuleRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneRuleRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneRuleDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneRuleRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneRuleServiceImpl implements MilestoneRuleService {

    private final MilestoneRuleRepository milestoneRuleRepository;
    private final MilestoneRuleDtoMapper milestoneRuleDtoMapper;
    private final CreateMilestoneRuleRequestMapper createMilestoneRuleRequestMapper;
    private final UpdateMilestoneRuleRequestMapper updateMilestoneRuleRequestMapper;
    private final MilestoneRepository milestoneRepository;

    @Override
    public BaseRepository<MilestoneRuleEntity> getRepository() {
        return milestoneRuleRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneRuleEntity, MilestoneRuleDto> getMapper() {
        return milestoneRuleDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneRuleDto> findAll(Pageable pageable) {
        return milestoneRuleRepository.findAll(pageable).map(milestoneRuleDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneRuleDto create(CreateMilestoneRuleRequest request) {
        log.info("Создание правила этапа: этап={}, режим оценки={}",
                request.getMilestoneId(), request.getAssessmentMode());

        Preconditions.checkArgument(request.getMilestoneId() != null, "Milestone ID не может быть null");

        // Валидация roundContestantLimit
        validateRoundContestantLimit(request.getContestantLimit(), request.getRoundContestantLimit());

        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(request.getMilestoneId());

        if (milestone.getMilestoneRule() != null) {
            log.warn("Попытка создания правила для этапа={}, у которого уже есть правило", milestone.getId());
            throw new IllegalArgumentException("У этапа уже есть правило");
        }

        log.debug("Найден этап={} для создания правила", milestone.getId());

        MilestoneRuleEntity rule = createMilestoneRuleRequestMapper.toEntity(request);
        rule.setMilestone(milestone);

        MilestoneRuleEntity saved = milestoneRuleRepository.save(rule);
        log.debug("Правило этапа сохранено с id={}", saved.getId());

        milestone.setMilestoneRule(saved);
        milestoneRepository.save(milestone);
        log.info("Правило этапа успешно создано с id={} для этапа={}", saved.getId(), milestone.getId());
        return milestoneRuleDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneRuleDto update(Long id, UpdateMilestoneRuleRequest request) {
        Preconditions.checkArgument(id != null, "ID правила не может быть null");

        MilestoneRuleEntity rule = milestoneRuleRepository.getByIdOrThrow(id);

        Integer participantLimit = request.getContestantLimit() != null ?
            request.getContestantLimit() : rule.getContestantLimit();
        Integer roundParticipantLimit = request.getRoundContestantLimit() != null ?
            request.getRoundContestantLimit() : rule.getRoundContestantLimit();

        validateRoundContestantLimit(participantLimit, roundParticipantLimit);

        updateMilestoneRuleRequestMapper.updateMilestoneRuleFromRequest(request, rule);

        MilestoneRuleEntity saved = milestoneRuleRepository.save(rule);
        return milestoneRuleDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Preconditions.checkArgument(id != null, "ID правила не может быть null");
        MilestoneRuleEntity rule = milestoneRuleRepository.getByIdOrThrow(id);

        // Разрываем связь, чтобы Hibernate не блокировал удаление
        MilestoneEntity milestone = rule.getMilestone();
        if (milestone != null) {
            milestone.setMilestoneRule(null);
        }

        milestoneRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public MilestoneRuleDto findByMilestoneId(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");

        MilestoneRuleEntity rule = milestoneRuleRepository.getByMilestoneIdOrThrow(milestoneId);

        return milestoneRuleDtoMapper.toDto(rule);
    }

    private void validateRoundContestantLimit(Integer contestantLimit, Integer roundContestantLimit) {
        log.debug("Валидация лимитов участников: лимит этапа={}, лимит раунда={}",
                contestantLimit, roundContestantLimit);

        if (contestantLimit != null && roundContestantLimit != null &&
            roundContestantLimit > contestantLimit) {
            log.warn("Недопустимые лимиты: лимит раунда={} больше лимита этапа={}",
                    roundContestantLimit, contestantLimit);
            throw new IllegalArgumentException("roundContestantLimit должен быть меньше или равен contestantLimit");
        }

        log.debug("Валидация лимитов участников прошла успешно");
    }
}
