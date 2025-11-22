package org.bn.sensation.core.milestone.service;

import java.util.*;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.entity.ContestantType;
import org.bn.sensation.core.contestant.repository.ContestantRepository;
import org.bn.sensation.core.contestant.service.ContestantService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneDto;
import org.bn.sensation.core.milestone.service.dto.PrepareRoundsRequest;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneRequestMapper;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.MilestoneResultService;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.service.RoundService;
import org.bn.sensation.core.round.service.RoundStateMachineService;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.statemachine.RoundEvent;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

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
    private final MilestoneResultService milestoneResultService;
    private final MilestoneStateMachineService milestoneStateMachineService;
    private final ParticipantRepository participantRepository;
    private final RoundService roundService;
    private final RoundStateMachineService roundStateMachineService;
    private final UpdateMilestoneRequestMapper updateMilestoneRequestMapper;
    private final ContestantService contestantService;
    private final ContestantRepository contestantRepository;

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
        log.debug("Поиск всех этапов с пагинацией: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        List<MilestoneEntity> milestones = milestoneRepository.findAll();
        log.debug("Найдено {} этапов в базе данных", milestones.size());

        List<MilestoneDto> enrichedDtos = milestones.stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), enrichedDtos.size());
        List<MilestoneDto> pageContent = enrichedDtos.subList(start, end);

        log.debug("Возвращается страница с {} этапами", pageContent.size());
        return new PageImpl<>(pageContent, pageable, enrichedDtos.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MilestoneDto> findById(Long id) {
        log.debug("Поиск этапа по id={}", id);
        Optional<MilestoneDto> result = milestoneRepository.findById(id)
                .map(this::enrichMilestoneDtoWithStatistics);
        if (result.isPresent()) {
            log.debug("Этап найден: id={}, название={}", id, result.get().getName());
        } else {
            log.debug("Этап не найден: id={}", id);
        }
        return result;
    }

    @Override
    @Transactional
    public MilestoneDto create(CreateMilestoneRequest request) {
        log.info("Создание этапа: название={}, активность={}", request.getName(), request.getActivityId());
        Preconditions.checkArgument(request.getActivityId() != null, "ID активности не может быть null");
        ActivityEntity activity = activityRepository.getByIdWithActivityUserOrThrow(request.getActivityId());
        log.debug("Найдена активность={} для создания этапа", activity.getId());
        Preconditions.checkState(activity.getState() == ActivityState.PLANNED, "Нельзя создать этап, т.к. активность в состоянии " + activity.getState());

        MilestoneEntity milestone = createMilestoneRequestMapper.toEntity(request);
        milestone.setState(MilestoneState.DRAFT);
        milestone.setActivity(activity);

        milestone.setMilestoneOrder(activity.getMilestones().size());
        log.debug("Установлен автоматический порядок этапа: {}", milestone.getMilestoneOrder());

        MilestoneEntity saved = milestoneRepository.save(milestone);
        log.info("Этап успешно создан: id={}, название={}", saved.getId(), saved.getName());

        return enrichMilestoneDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public MilestoneDto update(Long id, UpdateMilestoneRequest request) {
        log.info("Обновление этапа: id={}, название={}", id, request.getName());
        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(id);
        log.debug("Найден этап={} для обновления", milestone.getId());

        updateMilestoneRequestMapper.updateMilestoneFromRequest(request, milestone);

        MilestoneEntity saved = milestoneRepository.save(milestone);
        log.info("Этап успешно обновлен: id={}", saved.getId());
        return enrichMilestoneDtoWithStatistics(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление этапа: id={}", id);
        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(id);
        Integer order = milestone.getMilestoneOrder();
        Set<MilestoneEntity> milestones = milestoneRepository.findByActivityIdAndGtMilestoneOrder(milestone.getActivity().getId(), order)
                .stream().sorted(Comparator.comparing(MilestoneEntity::getMilestoneOrder))
                .peek(m -> m.setMilestoneOrder(m.getMilestoneOrder() - 1))
                .collect(Collectors.toSet());

        milestoneRepository.deleteById(id);
        milestoneRepository.saveAll(milestones);
        log.info("Этап успешно удален: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDto> findByActivityId(Long id) {
        log.debug("Поиск этапов для активности={}", id);
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        List<MilestoneDto> result = milestoneRepository.findByActivityIdOrderByMilestoneOrderDesc(id).stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();
        log.debug("Найдено {} этапов для активности={}", result.size(), id);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneDto> findByActivityIdInLifeStates(Long id) {
        log.debug("Поиск этапов в жизненных состояниях для активности={}", id);
        Preconditions.checkArgument(id != null, "ID активности не может быть null");
        List<MilestoneDto> result = milestoneRepository.findByActivityIdAndStateIn(id, MilestoneState.LIFE_MILESTONE_STATES).stream()
                .map(this::enrichMilestoneDtoWithStatistics)
                .toList();
        log.debug("Найдено {} этапов в жизненных состояниях для активности={}", result.size(), id);
        return result;
    }

    private MilestoneDto enrichMilestoneDtoWithStatistics(MilestoneEntity milestone) {
        log.debug("Обогащение статистикой этапа={}", milestone.getId());

        MilestoneDto dto = milestoneDtoMapper.toDto(milestone);

        int completedCount = (int) milestone.getRounds().stream()
                .filter(round -> round.getState() == RoundState.CLOSED)
                .count();
        int totalCount = milestone.getRounds().size();

        log.debug("Статистика раундов для этапа={}: завершено={}, всего={}",
                milestone.getId(), completedCount, totalCount);

        dto.setCompletedRoundsCount(completedCount);
        dto.setTotalRoundsCount(totalCount);

        return dto;
    }

    @Override
    @Transactional
    public void skipMilestone(Long id) {
        log.info("Пропуск этапа: id={}", id);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(id);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.SKIP);
        if (milestone.getMilestoneOrder() != 0) {
            MilestoneEntity nextMilestone = milestoneRepository.getByActivityIdAndMilestoneOrderOrThrow(milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1);
            nextMilestone.setContestants(milestone.getContestants());
            milestoneRepository.save(nextMilestone);
        }
        log.info("Пропуск запущен: id={}", id);
    }

    @Override
    @Transactional
    public void draftMilestone(Long id) {
        log.info("Перевод этапа в черновик: id={}", id);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(id);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.DRAFT);
        log.info("Этап переведен в черновик: id={}", id);
    }

    @Override
    @Transactional
    public void planMilestone(Long id) {
        log.info("Планирование этапа: id={}", id);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(id);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.PLAN);
        log.info("Этап запланирован: id={}", id);
    }

    @Override
    @Transactional
    public List<RoundDto> prepareRounds(Long milestoneId, PrepareRoundsRequest request) {
        log.info("Начало подготовки раундов для этапа ID={}, количество конкурсантов в раунде={}",
                milestoneId, request.getRoundContestantLimit());
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        log.debug("Этап найден: ID={}, состояние={}, активность ID={}",
                milestone.getId(), milestone.getState(), milestone.getActivity().getId());
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.PREPARE_ROUNDS);
        List<RoundDto> rounds = roundService.generateRounds(milestone,false, request.getRoundContestantLimit());
        milestoneRepository.save(milestone);
        return rounds;
    }

    @Override
    public List<RoundDto> regenerateRounds(Long milestoneId, PrepareRoundsRequest request) {
        log.info("Перегенерация раундов для этапа ID={}, количество конкурсантов в раунде={}",
                milestoneId, request.getRoundContestantLimit());
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        log.debug("Этап найден: ID={}, состояние={}", milestone.getId(), milestone.getState());
        Preconditions.checkState(milestone.getState() == MilestoneState.PENDING, "Перегенерация раундов невозможна, т.к. этап в состоянии %s ", milestone.getState());
        List<RoundDto> rounds = roundService.generateRounds(milestone,true, request.getRoundContestantLimit());
        milestoneRepository.save(milestone);
        return rounds;
    }

    @Override
    @Transactional
    public void startMilestone(Long id) {
        log.info("Запуск этапа: id={}", id);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(id);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.START);
        log.info("Этап запущен: id={}", id);
    }

    @Override
    @Transactional
    public List<MilestoneResultDto> sumUpMilestone(Long id) {
        log.info("Подведение предварительных итогов этапа: id={}", id);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(id);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.SUM_UP);
        List<MilestoneResultDto> milestoneResultDtos = milestoneResultService.calculateResults(milestone);
        milestone.getRounds().stream().filter(round -> round.getState() != RoundState.CLOSED)
                .forEach(round -> {
                    roundStateMachineService.sendEvent(round, RoundEvent.CLOSE);
                });
        log.info("Предварительные итоги этапа подведены: id={}", id);
        return milestoneResultDtos;
    }

    @Override
    @Transactional
    public void completeMilestone(Long milestoneId, List<UpdateMilestoneResultRequest> request) {
        log.info("Завершение этапа: id={}", milestoneId);
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        log.debug("Найден этап={} для завершения, количество раундов={}", milestoneId, milestone.getRounds().size());
        List<MilestoneResultEntity> milestoneResults = milestoneResultService.acceptResults(milestone, request);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.COMPLETE);
        if (milestone.getMilestoneOrder().intValue() != 0) {
            assignToNextMilestone(milestone, milestoneResults);
        }
        log.info("Этап успешно завершен: id={}", milestoneId);
    }

    private void assignToNextMilestone(MilestoneEntity milestone, List<MilestoneResultEntity> milestoneResults) {
        MilestoneEntity nextMilestone = milestoneRepository.findByActivityIdAndMilestoneOrder(milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1)
                .orElseThrow(() -> new IllegalArgumentException("Нет следующего этапа после этапа ID %s, порядковый номер %s".formatted(milestone.getId(), milestone.getMilestoneOrder())));

        Set<ContestantEntity> contestants = milestoneResults
                .stream()
                .filter(mr -> Boolean.TRUE.equals(mr.getFinallyApproved()))
                .map(MilestoneResultEntity::getContestant)
                .collect(Collectors.toSet());
        log.info("Найдено конкурсантов для перевода в следующий этап: {}", contestants.size());
        Preconditions.checkArgument(!contestants.isEmpty(), "Нет конкурсантов для перевода в следующий этап");

        ContestantType type = milestone.getMilestoneRule().getContestantType();
        ContestantType nextType = nextMilestone.getMilestoneRule().getContestantType();
        if (type == nextType && nextType.isPersistent()) {
            nextMilestone.setContestants(contestants);
            contestants.forEach(c -> c.getMilestones().add(nextMilestone));
            contestantRepository.saveAll(contestants);
        } else {
            List<ParticipantEntity> participants = contestants.stream()
                    .flatMap(c -> c.getParticipants().stream())
                    .toList();
            contestantService.createContestants(nextMilestone, participants);
            participantRepository.saveAll(participants);
        }

        if (milestone.getMilestoneRule().getStrictPassMode()) {
            if (nextType.hasPartnerSide()) {
                Map<PartnerSide, List<ParticipantEntity>> sides = nextMilestone.getContestants().stream()
                        .flatMap(c -> c.getParticipants().stream())
                        .collect(Collectors.groupingBy(ParticipantEntity::getPartnerSide));
                Preconditions.checkArgument(Math.max(sides.getOrDefault(PartnerSide.FOLLOWER, List.of()).size(), sides.getOrDefault(PartnerSide.LEADER, List.of()).size())
                                <= milestone.getMilestoneRule().getContestantLimit().intValue(),
                        "Количество прошедших конкурсантов не может быть больше %s", milestone.getMilestoneRule().getContestantLimit());
            } else {
                Preconditions.checkArgument(nextMilestone.getContestants().size() <= milestone.getMilestoneRule().getContestantLimit().intValue(),
                        "Количество прошедших конкурсантов не может быть больше %s", milestone.getMilestoneRule().getContestantLimit());
            }
        }
        milestoneRepository.save(nextMilestone);
    }
}
