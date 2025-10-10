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

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

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
        Preconditions.checkArgument(request.getMilestoneId() != null, "Milestone ID не может быть null");

        // Валидация roundParticipantLimit
        validateRoundParticipantLimit(request.getParticipantLimit(), request.getRoundParticipantLimit());

        MilestoneEntity milestone = milestoneRepository.findById(request.getMilestoneId())
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));

        if (milestone.getMilestoneRule() != null) {
            throw new IllegalArgumentException("У этапа уже есть правило");
        }

        MilestoneRuleEntity rule = createMilestoneRuleRequestMapper.toEntity(request);
        rule.setMilestone(milestone);

        MilestoneRuleEntity saved = milestoneRuleRepository.save(rule);

        milestone.setMilestoneRule(saved);
        milestoneRepository.save(milestone);
        return milestoneRuleDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneRuleDto update(Long id, UpdateMilestoneRuleRequest request) {
        Preconditions.checkArgument(id != null, "ID правила не может быть null");

        MilestoneRuleEntity rule = milestoneRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Правило не найдено с id: " + id));

        Integer participantLimit = request.getParticipantLimit() != null ?
            request.getParticipantLimit() : rule.getParticipantLimit();
        Integer roundParticipantLimit = request.getRoundParticipantLimit() != null ? 
            request.getRoundParticipantLimit() : rule.getRoundParticipantLimit();

        validateRoundParticipantLimit(participantLimit, roundParticipantLimit);

        updateMilestoneRuleRequestMapper.updateMilestoneRuleFromRequest(request, rule);

        MilestoneRuleEntity saved = milestoneRuleRepository.save(rule);
        return milestoneRuleDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Preconditions.checkArgument(id != null, "ID правила не может быть null");
        MilestoneRuleEntity rule = milestoneRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Правило не найдено с id: " + id));

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

        MilestoneRuleEntity rule = milestoneRuleRepository.findByMilestoneId(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Правило не найдено для этапа с id: " + milestoneId));

        return milestoneRuleDtoMapper.toDto(rule);
    }

    private void validateRoundParticipantLimit(Integer participantLimit, Integer roundParticipantLimit) {
        if (participantLimit != null && roundParticipantLimit != null && 
            roundParticipantLimit > participantLimit) {
            throw new IllegalArgumentException("roundParticipantLimit должен быть меньше или равен participantLimit");
        }
    }
}
