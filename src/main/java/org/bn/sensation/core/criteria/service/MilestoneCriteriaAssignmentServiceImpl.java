package org.bn.sensation.core.criteria.service;

import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.CriteriaEntity;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.CriteriaRepository;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.criteria.service.dto.CreateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.criteria.service.dto.MilestoneCriteriaAssignmentDto;
import org.bn.sensation.core.criteria.service.dto.UpdateMilestoneCriteriaAssignmentRequest;
import org.bn.sensation.core.criteria.service.mapper.CreateMilestoneCriteriaAssignmentRequestMapper;
import org.bn.sensation.core.criteria.service.mapper.MilestoneCriteriaAssignmentDtoMapper;
import org.bn.sensation.core.criteria.service.mapper.UpdateMilestoneCriteriaAssignmentRequestMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

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

    @Override
    @Transactional
    public MilestoneCriteriaAssignmentDto create(CreateMilestoneCriteriaAssignmentRequest request) {
        Preconditions.checkArgument(request.getMilestoneId() != null, "Milestone ID не может быть null");
        Preconditions.checkArgument(request.getCriteriaId() != null, "Criteria ID не может быть null");

        // Проверяем, что назначение еще не существует
        if (milestoneCriteriaAssignmentRepository.existsByMilestoneIdAndCriteriaId(request.getMilestoneId(), request.getCriteriaId())) {
            throw new IllegalArgumentException("Критерий уже назначен на этот этап");
        }

        // Проверяем существование этапа
        MilestoneEntity milestone = milestoneRepository.findById(request.getMilestoneId())
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));

        // Проверяем существование критерия
        CriteriaEntity criteria = criteriaRepository.findById(request.getCriteriaId())
                .orElseThrow(() -> new EntityNotFoundException("Критерий не найден с id: " + request.getCriteriaId()));

        // Создаем сущность назначения
        MilestoneCriteriaAssignmentEntity assignment = createMilestoneCriteriaAssignmentRequestMapper.toEntity(request);
        assignment.setMilestone(milestone);
        assignment.setCriteria(criteria);

        MilestoneCriteriaAssignmentEntity saved = milestoneCriteriaAssignmentRepository.save(assignment);
        return milestoneCriteriaAssignmentDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneCriteriaAssignmentDto update(Long id, UpdateMilestoneCriteriaAssignmentRequest request) {
        Preconditions.checkArgument(id != null, "ID назначения не может быть null");

        MilestoneCriteriaAssignmentEntity assignment = milestoneCriteriaAssignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Назначение не найдено с id: " + id));

        // Обновляем поля назначения
        updateMilestoneCriteriaAssignmentRequestMapper.updateMilestoneCriteriaAssignmentFromRequest(request, assignment);

        // Обновляем этап если указан
        if (request.getMilestoneId() != null) {
            MilestoneEntity milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));
            assignment.setMilestone(milestone);
        }

        // Обновляем критерий если указан
        if (request.getCriteriaId() != null) {
            CriteriaEntity criteria = criteriaRepository.findById(request.getCriteriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Критерий не найден с id: " + request.getCriteriaId()));
            assignment.setCriteria(criteria);
        }

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
    public Page<MilestoneCriteriaAssignmentDto> findByMilestoneId(Long milestoneId, Pageable pageable) {
        Preconditions.checkArgument(milestoneId != null, "Milestone ID не может быть null");

        return milestoneCriteriaAssignmentRepository.findByMilestoneId(milestoneId, pageable).map(milestoneCriteriaAssignmentDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneCriteriaAssignmentDto> findByCriteriaId(Long criteriaId, Pageable pageable) {
        Preconditions.checkArgument(criteriaId != null, "Criteria ID не может быть null");

        return milestoneCriteriaAssignmentRepository.findByCriteriaId(criteriaId, pageable).map(milestoneCriteriaAssignmentDtoMapper::toDto);
    }

}
