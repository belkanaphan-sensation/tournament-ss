package org.bn.sensation.core.milestone.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneEntity;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.JudgeMilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneDto;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.JudgeMilestoneDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneRequestMapper;
import org.bn.sensation.core.round.entity.JudgeRoundStatus;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.JudgeRoundRepository;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.security.CurrentUser;
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

    private final MilestoneRepository milestoneRepository;
    private final MilestoneDtoMapper milestoneDtoMapper;
    private final CreateMilestoneRequestMapper createMilestoneRequestMapper;
    private final UpdateMilestoneRequestMapper updateMilestoneRequestMapper;
    private final ActivityRepository activityRepository;
    private final JudgeMilestoneRepository judgeMilestoneRepository;
    private final JudgeMilestoneDtoMapper judgeMilestoneDtoMapper;
    private final JudgeRoundRepository judgeRoundRepository;
    private final CurrentUser currentUser;

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
        List<MilestoneEntity> milestones = milestoneRepository.findAll();
        List<MilestoneDto> enrichedDtos = milestones.stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), enrichedDtos.size());
        List<MilestoneDto> pageContent = enrichedDtos.subList(start, end);

        return new PageImpl<>(pageContent, pageable, enrichedDtos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MilestoneDto> findById(Long id) {
        return milestoneRepository.findById(id)
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
            throw new EntityNotFoundException("Этап не найден с id: " + id);
        }
        milestoneRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDto> findByActivityId(Long id) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        return milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(id).stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDto> findByActivityIdInLifeStates(Long id) {
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        return milestoneRepository.findByActivityIdAndStateIn(id, MilestoneState.LIFE_MILESTONE_STATES).stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();
    }

    private MilestoneDto enrichMilestoneDtoWithStatistics(MilestoneEntity milestone) {
        MilestoneDto dto = milestoneDtoMapper.toDto(milestone);
        dto.setCompletedRoundsCount((int) milestone.getRounds().stream()
                .filter(round -> round.getState() == RoundState.COMPLETED)
                .count());
        dto.setTotalRoundsCount(milestone.getRounds().size());

        return dto;
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

    @Override
    @Transactional
    public JudgeMilestoneDto changeMilestoneStatus(Long milestoneId, JudgeMilestoneStatus judgeMilestoneStatus) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        Preconditions.checkArgument(judgeMilestoneStatus != null, "Статус не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdWithUserAssignments(milestoneId).orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к этапу с id: %s".formatted(currentUser.getSecurityUser().getId(), milestoneId)));
        Preconditions.checkState(milestone.getState() == MilestoneState.IN_PROGRESS,
                "Статус этапа %s. Не может быть принят или отменен судьей", milestone.getState());

        if (!canChange(activityUser.getId(), milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList(), judgeMilestoneStatus)) {
            throw new IllegalStateException("Не все результаты раундов готовы");
        }

        return changeMilestoneStatus(milestone, activityUser, judgeMilestoneStatus);
    }

    @Override
    @Transactional
    public JudgeMilestoneDto changeMilestoneStatus(MilestoneEntity milestone, UserActivityAssignmentEntity activityUser, JudgeMilestoneStatus judgeMilestoneStatus) {
        JudgeMilestoneEntity judgeMilestoneEntity = judgeMilestoneRepository.findByMilestoneIdAndJudgeId(milestone.getId(), activityUser.getId())
                .orElse(JudgeMilestoneEntity.builder().milestone(milestone).judge(activityUser).build());
        judgeMilestoneEntity.setStatus(judgeMilestoneStatus);
        return judgeMilestoneDtoMapper.toDto(judgeMilestoneRepository.save(judgeMilestoneEntity));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean allRoundsReady(Long milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findByIdWithUserAssignments(milestoneId).orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к этапу с id: %s".formatted(currentUser.getSecurityUser().getId(), milestoneId)));

        return canChange(activityUser.getId(), milestone.getRounds().stream().map(RoundEntity::getId).distinct().toList(), JudgeMilestoneStatus.READY);
    }

    private boolean canChange(Long activityUserId, List<Long> roundIds, JudgeMilestoneStatus judgeMilestoneStatus) {
        if (judgeMilestoneStatus == JudgeMilestoneStatus.READY) {
            int readyRounds = judgeRoundRepository.countByJudgeIdAndStatusAndRoundIdIn(activityUserId, JudgeRoundStatus.READY, roundIds);
            return readyRounds == roundIds.size();
        }
        return true;
    }

    @Override
    public void saveTransition(MilestoneEntity milestone, MilestoneState state) {
        milestone.setState(state);
        milestoneRepository.save(milestone);
    }

    @Override
    public boolean canTransition(MilestoneEntity milestone, MilestoneEvent event) {
        switch (event) {
            case PLAN -> {
            }
            case START -> {
                Preconditions.checkState(milestone.getActivity().getState() == ActivityState.IN_PROGRESS,
                        "Нельзя стартовать этап, т.к. активность находится в статусе %s", milestone.getActivity().getState());
            }
            case COMPLETE -> {
                boolean allRoundsCompleted = milestone.getRounds()
                        .stream()
                        .allMatch(round -> round.getState() == RoundState.COMPLETED);
                Preconditions.checkState(allRoundsCompleted, "Не все раунды завершены");
                AssessmentMode assessmentMode = milestone.getMilestoneRule().getAssessmentMode();
                //должны быть посчитаны результаты этапа - проверка
                //это должно уйтив проверку подсчетов результата этапа
                if (assessmentMode == AssessmentMode.PASS) {
                    //Нужно проверить что каждый судья выбрал ровное необходимое количество участников
                    //где-то нужно хранить количество пользователей следующего этапа
                    //т.е. для каждого судьи должно быть необходимое количество ParticipantRoundResultEntity
                    //учесть пол участников и судей
                }
            }
        }
        return true;
    }

    @Override
    public MilestoneState getNextState(MilestoneState currentState, MilestoneEvent event) {
        return switch (currentState) {
            case DRAFT -> event == MilestoneEvent.PLAN ? MilestoneState.PLANNED : currentState;
            case PLANNED, COMPLETED -> event == MilestoneEvent.START ? MilestoneState.IN_PROGRESS : currentState;
            case IN_PROGRESS -> event == MilestoneEvent.COMPLETE ? MilestoneState.COMPLETED : currentState;
        };
    }

    @Override
    public boolean isValidTransition(MilestoneState currentState, MilestoneEvent event) {
        return getNextState(currentState, event) != currentState;
    }
}
