package org.bn.sensation.core.milestone.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneRequestMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.service.RoundStateMachineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class MilestoneServiceImpl implements MilestoneService {

    private final ActivityRepository activityRepository;
    private final CreateMilestoneRequestMapper createMilestoneRequestMapper;
    private final MilestoneDtoMapper milestoneDtoMapper;
    private final MilestoneRepository milestoneRepository;
//    private final MilestoneStateMachineService milestoneStateMachineService;
    private final ParticipantRepository participantRepository;
    private final RoundStateMachineService roundStateMachineService;
    private final UpdateMilestoneRequestMapper updateMilestoneRequestMapper;

    @Override
    public BaseRepository<MilestoneEntity> getRepository() {
        return milestoneRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneEntity, MilestoneDto> getMapper() {
        return milestoneDtoMapper;
    }

    @Override
    public void completeMilestone(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(milestoneId);
        milestone.getRounds().forEach(round -> roundStateMachineService.sendEvent(round.getId(), RoundEvent.COMPLETE));
//        milestoneStateMachineService.sendEvent(milestoneId, MilestoneEvent.COMPLETE);
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
        addParticipants(request.getParticipantIds(), activity, milestone);
        MilestoneEntity saved = milestoneRepository.save(milestone);

        return enrichMilestoneDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public MilestoneDto update(Long id, UpdateMilestoneRequest request) {
        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(id);

        Integer oldOrder = milestone.getMilestoneOrder();

        if (request.getMilestoneOrder() != null) {
            validateOrderSequence(milestone.getActivity().getId(), request.getMilestoneOrder(), false);
            milestone.setMilestoneOrder(request.getMilestoneOrder());

            if (!request.getMilestoneOrder().equals(oldOrder)) {
                reorderMilestones(milestone.getActivity().getId(), id, request.getMilestoneOrder());
            }
        }
        updateMilestoneRequestMapper.updateMilestoneFromRequest(request, milestone);
        addParticipants(request.getParticipantIds(), milestone.getActivity(), milestone);

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
        log.debug("Обогащение статистикой этапа={}", milestone.getId());

        MilestoneDto dto = milestoneDtoMapper.toDto(milestone);

        int completedCount = (int) milestone.getRounds().stream()
                .filter(round -> round.getState() == RoundState.COMPLETED)
                .count();
        int totalCount = milestone.getRounds().size();

        log.debug("Статистика раундов для этапа={}: завершено={}, всего={}",
                milestone.getId(), completedCount, totalCount);

        dto.setCompletedRoundsCount(completedCount);
        dto.setTotalRoundsCount(totalCount);

        return dto;
    }

    /**
     * Валидирует, что порядок последовательный (нельзя проставить порядок 5 если не существует этапов с порядками меньше)
     */
    private void validateOrderSequence(Long activityId, Integer newOrder, boolean create) {
        log.debug("Валидация порядка этапа: активность={}, новый порядок={}, создание={}",
                activityId, newOrder, create);

        Integer maxOrder = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(activityId).stream()
                .map(MilestoneEntity::getMilestoneOrder)
                .max(Integer::compareTo)
                .orElse(-1);

        int maxNewOrder = create ? maxOrder + 1 : maxOrder;

        log.debug("Максимальный существующий порядок={}, максимальный новый порядок={}",
                maxOrder, maxNewOrder);

        if (newOrder > maxNewOrder) {
            log.warn("Недопустимый порядок этапа: запрошен={}, максимальный={}", newOrder, maxNewOrder);
            throw new IllegalArgumentException("Нельзя установить порядок " + newOrder +
                    ". Максимальный существующий порядок: " + maxOrder +
                    ". Можно установить порядок от 0 до " + maxNewOrder);
        }

        log.debug("Порядок этапа валиден");
    }

    /**
     * Пересчитывает порядок всех этапов активности при изменении порядка одного этапа
     */
    private void reorderMilestones(Long activityId, Long currentMilestoneId, Integer newOrder) {
        log.debug("Пересчет порядка этапов: активность={}, текущий этап={}, новый порядок={}",
                activityId, currentMilestoneId, newOrder);

        List<MilestoneEntity> milestones = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(activityId)
                .stream()
                .filter(m -> currentMilestoneId == null || !m.getId().equals(currentMilestoneId)) // Исключаем текущий этап (если указан)
                .collect(Collectors.toList());

        log.debug("Найдено {} этапов для пересчета порядка", milestones.size());

        for (int i = 0; i < milestones.size(); i++) {
            MilestoneEntity milestone = milestones.get(i);
            Integer oldOrder = milestone.getMilestoneOrder();
            if (i >= newOrder) {
                milestone.setMilestoneOrder(i + 1);
            } else {
                milestone.setMilestoneOrder(i);
            }
            log.debug("Этап={}: порядок изменен с {} на {}", milestone.getId(), oldOrder, milestone.getMilestoneOrder());
        }
        milestoneRepository.saveAll(milestones);
        log.debug("Порядок этапов пересчитан и сохранен");
    }

    /**
     * Рассчитывает следующий порядковый номер для этапа в рамках активности
     */
    private Integer calculateNextOrder(Long activityId) {
        log.debug("Расчет следующего порядка для активности={}", activityId);

        Integer nextOrder = milestoneRepository.findByActivityIdOrderByMilestoneOrderAsc(activityId)
                .stream()
                .map(MilestoneEntity::getMilestoneOrder)
                .max(Integer::compareTo)
                .map(max -> max + 1)
                .orElse(0);

        log.debug("Следующий порядок для активности={}: {}", activityId, nextOrder);
        return nextOrder;
    }

    @Override
    public void saveTransition(MilestoneEntity milestone, MilestoneState state) {
        milestone.setState(state);
        milestoneRepository.save(milestone);
    }

    @Override
    public boolean canTransition(MilestoneEntity milestone, MilestoneEvent event) {
        switch (event) {
            case DRAFT, PLAN -> {
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
                int resultsCount = milestone.getResults().size();
                long participantsCount = milestone.getRounds().stream()
                        .filter(round -> !round.getExtraRound())
                        .flatMap(round -> round.getParticipants().stream())
                        .count();
                Preconditions.checkState(resultsCount == participantsCount,
                        "Результаты готовы не для всех участников");
            }
        }
        return true;
    }

    @Override
    public MilestoneState getNextState(MilestoneState currentState, MilestoneEvent event) {
        return switch (currentState) {
            case DRAFT -> event == MilestoneEvent.PLAN ? MilestoneState.PLANNED : currentState;
            case PLANNED -> switch (event) {
                case DRAFT -> MilestoneState.DRAFT;
                case START -> MilestoneState.IN_PROGRESS;
                default -> currentState;
            };
            case IN_PROGRESS -> switch (event) {
                case PLAN -> MilestoneState.PLANNED;
                case COMPLETE -> MilestoneState.COMPLETED;
                default -> currentState;
            };
            case COMPLETED -> event == MilestoneEvent.START ? MilestoneState.IN_PROGRESS : currentState;
            default -> currentState;
        };
    }

    @Override
    public boolean isValidTransition(MilestoneState currentState, MilestoneEvent event) {
        return getNextState(currentState, event) != currentState;
    }

    /**
     * Не должно применяться в нормальном флоу. Нужно на экстренный случай
     */
    private void addParticipants(List<Long> participantIds, ActivityEntity activity, MilestoneEntity milestone) {
        if (participantIds != null && !participantIds.isEmpty()) {
            Set<ParticipantEntity> participants = participantRepository.findAllByIdWithActivity(participantIds)
                    .stream()
                    .peek(participant -> {
                        Preconditions.checkArgument(participant.getIsRegistered(), "Может быть добавлен только зарегистрированный участник");
                        Preconditions.checkArgument(participant.getActivity().getId().equals(activity.getId()),
                                "Участник с ID %s не принадлежит активности %s", participant.getId(), activity.getId());
                    })
                    .collect(Collectors.toSet());
            milestone.getParticipants().addAll(participants);
        }
    }
}
