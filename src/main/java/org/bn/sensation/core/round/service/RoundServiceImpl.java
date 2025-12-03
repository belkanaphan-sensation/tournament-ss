package org.bn.sensation.core.round.service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.repository.ContestantRepository;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.judgeroundstatus.service.JudgeRoundStatusService;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.MilestoneStateMachineService;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.bn.sensation.core.round.service.mapper.RoundWithJRStatusMapper;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.sse.NotificationService;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundServiceImpl implements RoundService {

    private final CurrentUser currentUser;
    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final JudgeRoundStatusService judgeRoundStatusService;
    private final MilestoneRepository milestoneRepository;
    private final RoundDtoMapper roundDtoMapper;
    private final RoundRepository roundRepository;
    private final RoundWithJRStatusMapper roundWithJRStatusMapper;
    private final ContestantRepository contestantRepository;
    private final MilestoneStateMachineService milestoneStateMachineService;
    private final NotificationService notificationService;

    @Override
    public BaseRepository<RoundEntity> getRepository() {
        return roundRepository;
    }

    @Override
    public BaseDtoMapper<RoundEntity, RoundDto> getMapper() {
        return roundDtoMapper;
    }

    @Override
    @Transactional
    public RoundDto createExtraRound(CreateRoundRequest request) {
        log.info("Создание захода: название={}, этап={}", request.getName(), request.getMilestoneId());

        // Проверяем существование этапа
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(request.getMilestoneId());
        log.debug("Найден этап={} для создания захода", milestone.getId());
        Preconditions.checkState(milestone.getState() == MilestoneState.SUMMARIZING,
                "Невозможно создать дополнительный заход, т.к. этап в состоянии %s".formatted(milestone.getState()));
        if (milestone.getMilestoneOrder().equals(0) && !milestone.getRounds().isEmpty()) {
            throw new IllegalArgumentException("заход не может быть создан для этапа %s, т.к этап финальный и уже имеет заход".formatted(milestone.getId()));
        }

        // Создаем сущность захода
        RoundEntity round = new RoundEntity();
        String name = Strings.isNullOrEmpty(request.getName())
                ? "*Заход " + (milestone.getRounds().size() + 1)
                : request.getName();
        round.setName(name);
        round.setExtraRound(true);
        round.setState(RoundState.OPENED);
        round.setMilestone(milestone);
        round.setRoundOrder(milestone.getRounds().size());
        checkContestants(request.getContestantIds(), milestone);
        addContestants(request.getContestantIds(), milestone, round);

        RoundEntity saved = roundRepository.save(round);
        log.info("Сохранен заход с id={}", saved.getId());

        // Создаем статусы для судей
        createJudgeStatusesForRound(milestone, saved);

        // Инвалидируем кэш статуса этапа при создании новых статусов судей
        Long milestoneId = milestone.getId();
        judgeMilestoneStatusCacheService.invalidateForMilestone(milestoneId);
        log.debug("Инвалидирован кэш статуса этапа milestoneId={} после создания статусов судей для нового захода", milestoneId);
        milestoneStateMachineService.sendEvent(milestone, MilestoneEvent.START);
        milestone.getActivity().getActivityUsers().forEach(
                au -> notificationService.sendNotificationToUser(
                        au.getUser().getId(),
                        "Создан перетанцовочный раунд этапа %s".formatted(milestone.getName()))
        );
        return roundDtoMapper.toDto(saved);
    }

    private void checkContestants(@NotNull List<Long> contestantIds, MilestoneEntity milestone) {
        contestantIds.forEach(pId -> {
            Boolean finallyApproved = Optional.ofNullable(milestone.getResults()
                    .stream().collect(Collectors.toMap(
                            res -> res.getContestant().getId(),
                            res -> res.getFinallyApproved()))
                    .get(pId)).orElseThrow(() -> new IllegalArgumentException("Для конкурсанта с ID %s не существует результата этапа".formatted(pId)));
            Preconditions.checkArgument(!Boolean.TRUE.equals(finallyApproved),
                    "Конкурсант %s прошел в следующий этап по результатам основного захода и не может быть добавлен в дополнительный"
                            .formatted(pId));
        });
    }

    private void addContestants(List<Long> contestantIds, MilestoneEntity milestone, RoundEntity round) {
        log.warn("Добавление конкурсантов в заход: milestone={}, round={}, contestantIds={}", milestone.getId(), round.getId(), contestantIds);
        Set<ContestantEntity> contestants = contestantRepository.findAllByIdFull(contestantIds)
                .stream()
                .peek(contestant -> {
                    Preconditions.checkArgument(contestant.getMilestones().stream().anyMatch(m -> m.getId().equals(milestone.getId())),
                            "Конкурсант с ID %s должен быть сначала добавлен в этап захода с ID: %s", contestant.getId(), milestone.getId());
                })
                .collect(Collectors.toSet());
        Preconditions.checkArgument(contestantIds.size() == contestants.size(), "Не все из заявленных конкурсантов найдены");
        // Обновляем обе стороны связи ManyToMany: добавляем round в participant.rounds
        contestants.forEach(contestant -> contestant.getRounds().add(round));
        round.getContestants().addAll(contestants);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Preconditions.checkArgument(id != null, "ID захода не может быть null");
        RoundEntity round = roundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("заход не найден с id: " + id));
        Integer roundOrder = round.getRoundOrder();
        Set<RoundEntity> reordered = roundRepository.findByMilestoneIdAndGtRoundOrder(round.getMilestone().getId(), roundOrder)
                .stream().sorted(Comparator.comparing(RoundEntity::getRoundOrder))
                .peek(r -> r.setRoundOrder(r.getRoundOrder() - 1))
                .collect(Collectors.toSet());
        roundRepository.deleteById(id);
        roundRepository.saveAll(reordered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundDto> findByMilestoneId(Long id) {
        Preconditions.checkArgument(id != null, "ID этапа не может быть null");
        return roundRepository.findByMilestoneId(id).stream()
                .sorted(Comparator.comparing(RoundEntity::getRoundOrder))
                .map(roundDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundWithJRStatusDto> findByMilestoneIdInLifeStates(Long milestoneId) {
        log.info("Поиск заходов в жизненных состояниях для этапа={}, пользователь={}",
                milestoneId, currentUser.getSecurityUser().getId());

        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);

        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity judge = ActivityUserUtil.getFromActivity(
                milestone.getActivity(), userId, ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge());

        log.debug("Найден судья={} для этапа={}", judge.getId(), milestoneId);

        Map<Long, JudgeRoundStatusEntity> judgeRoundEntityMap = judgeRoundStatusRepository.findByMilestoneIdAndJudgeId(milestoneId, judge.getId())
                .stream()
                .collect(Collectors.toMap(jre -> jre.getRound().getId(), Function.identity()));

        log.debug("Найдено {} статусов судьи для этапа={}", judgeRoundEntityMap.size(), milestoneId);

        List<RoundWithJRStatusDto> result = milestone.getRounds()
                .stream()
                .filter(round -> {
                    boolean isLifeState = RoundState.LIFE_ROUND_STATES.contains(round.getState());
                    log.debug("заход={} со статусом={} в жизненных состояниях: {}",
                            round.getId(), round.getState(), isLifeState);
                    return isLifeState;
                })
                .map(round -> roundWithJRStatusMapper.toDto(round, judgeRoundEntityMap.get(round.getId())))
                .sorted(Comparator.comparing(RoundWithJRStatusDto::getRoundOrder))
                .toList();

        log.info("Найдено {} заходов в жизненных состояниях для этапа={}", result.size(), milestoneId);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<RoundDto> generateRounds(@NotNull MilestoneEntity milestone, boolean reGenerate, Integer roundParticipantLimit) {
        Preconditions.checkArgument(!milestone.getContestants().isEmpty(), "Отсутствуют конкурсанты для генерации заходов");
        if (!reGenerate) {
            Preconditions.checkArgument(milestone.getRounds().isEmpty(), "Этап уже содержит заходы");
        } else {
            // Очищаем связи у конкурсантов
            milestone.getContestants().forEach(contestant -> 
                contestant.getRounds().removeAll(milestone.getRounds())
            );
            // Удаляем заходы
            roundRepository.deleteAll(milestone.getRounds());
            milestone.getRounds().clear();
        }

        int roundLimit = roundParticipantLimit == null ? milestone.getMilestoneRule().getRoundContestantLimit().intValue() : roundParticipantLimit;
        List<RoundEntity> rounds = roundRepository.saveAll(generate(milestone, roundLimit));
        milestone.getRounds().addAll(rounds);

        // Сохраняем участников с обновленными связями
        contestantRepository.saveAll(milestone.getContestants());
        rounds.forEach(r -> createJudgeStatusesForRound(milestone, r));

        log.info("Успешно сгенерировано {} заходов для этапа ID={}", rounds.size(), milestone.getId());
        return rounds.stream().sorted(Comparator.comparing(RoundEntity::getRoundOrder)).map(roundDtoMapper::toDto).toList();
    }

    private List<RoundEntity> generate(MilestoneEntity milestone, int roundLimit) {
        log.debug("Начало генерации заходов для {} конкурсантов", milestone.getContestants().size());
        //TODO ограничение для strictPassMode у следующего этапа
        boolean isLastMilestone = milestone.getMilestoneOrder().intValue() == 0;
        boolean isAssessmentPlaces = milestone.getMilestoneRule().getAssessmentMode() == AssessmentMode.PLACE;
        boolean isStrictPassMode = false;
        if (!isLastMilestone) {
            MilestoneEntity nextMilestone = milestoneRepository.getByActivityIdAndMilestoneOrderOrThrow(milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1);
            isStrictPassMode = nextMilestone.getMilestoneRule().getStrictPassMode();
        }
        if (isLastMilestone || isStrictPassMode || isAssessmentPlaces) {
            log.debug("Генерация финального захода. Количество конкурсантов {} лимит финального этапа {}",
                    milestone.getContestants().size(), milestone.getMilestoneRule().getContestantLimit());
            RoundEntity finalRound = RoundEntity.builder()
                    .roundOrder(0)
                    .extraRound(false)
                    .milestone(milestone)
                    .state(RoundState.OPENED)
                    .name(milestone.getName())
                    .contestants(new HashSet<>(milestone.getContestants()))
                    .build();
            // Обновляем обе стороны связи ManyToMany: добавляем round в participant.rounds
            milestone.getContestants().forEach(contestant -> contestant.getRounds().add(finalRound));
            return List.of(finalRound);
        }

        log.debug("Лимит конкурсантов на заход: {}", roundLimit);
        List<ContestantEntity> contestants = new ArrayList<>(milestone.getContestants());
        List<ContestantEntity> leaders = new ArrayList<>();
        List<ContestantEntity> followers = new ArrayList<>();
        if (milestone.getMilestoneRule().getContestantType().hasPartnerSide()) {
            leaders.addAll(contestants.stream()
                    .filter(c -> c.getParticipants().iterator().next().getPartnerSide() == PartnerSide.LEADER)
                    .toList());
            followers.addAll(contestants.stream()
                    .filter(c -> c.getParticipants().iterator().next().getPartnerSide() == PartnerSide.FOLLOWER)
                    .toList());
            log.debug("Разделение конкурсантов: лидеров={}, последователей={}", leaders.size(), followers.size());
        }

        int dividend = milestone.getMilestoneRule().getContestantType().hasPartnerSide()
                ? Math.max(leaders.size(), followers.size())
                : contestants.size();
        int roundCount = dividend / roundLimit;
        int remainder = dividend % roundLimit;
        log.debug("Расчет заходов: всего конкурсантов={}, заходов={}, остаток={}",
                dividend, roundCount, remainder);
        if (remainder > 0) {
            roundCount++;
            log.debug("Создается дополнительный заход для остатка");
        }
        log.info("Будет создано {} заходов", roundCount);

        List<RoundEntity> roundEntities = new ArrayList<>();
        for (int i = 0; i < roundCount; i++) {
            RoundEntity round = RoundEntity.builder()
                    .roundOrder(i)
                    .extraRound(false)
                    .milestone(milestone)
                    .state(RoundState.OPENED)
                    .name("Заход %s".formatted(i + 1))
                    .build();

            log.debug("Создание захода {}", round.getName());
            int j = 0;
            if (milestone.getMilestoneRule().getContestantType().hasPartnerSide()) {
                while (j < roundLimit && (!leaders.isEmpty() || !followers.isEmpty())) {
                    addToRound(leaders, round);
                    addToRound(followers, round);
                    j++;
                    log.trace("Добавлены конкурсанты в заход {}, всего конкурсантов распределенных по сторонам: {}", i + 1, round.getContestants().size());
                }
            } else {
                while (j < roundLimit && !contestants.isEmpty()) {
                    addToRound(contestants, round);
                    j++;
                    log.trace("Добавлен конкурсант в заход {}, всего конкурсантов: {}", i + 1, round.getContestants().size());
                }
            }
            log.debug("Заход {} сформирован: {} конкурсантов", i, round.getContestants().size());
            roundEntities.add(round);
        }
        log.info("Генерация завершена: создано {} заходов", roundEntities.size());
        return roundEntities;
    }

    private void addToRound(List<ContestantEntity> contestants, RoundEntity round) {
        if (!contestants.isEmpty()) {
            ContestantEntity contestant = contestants.remove(0);
            contestant.getRounds().add(round);
            round.getContestants().add(contestant);
            log.trace("Добавлен конкурсант ID={} в заход {}", contestant.getId(), round.getName());
        }
    }

    private void createJudgeStatusesForRound(MilestoneEntity milestone, RoundEntity round) {
        int judgeStatusCount = 0;
        List<JudgeRoundStatusEntity> judgeRoundStatusEntities = new ArrayList<>();
        for (ActivityUserEntity au : milestone.getActivity().getActivityUsers()) {
            if (au.getPosition().isJudge()) {
                JudgeRoundStatus status = JudgeRoundStatus.NOT_READY;
                if (au.getPartnerSide() != null && milestone.getMilestoneRule().getContestantType().hasPartnerSide()
                        && round.getContestants().stream().filter(c -> c.getParticipants().iterator().next().getPartnerSide() == au.getPartnerSide()).count() == 0) {
                    status = JudgeRoundStatus.READY;
                }
                JudgeRoundStatusEntity judgeRoundStatus = JudgeRoundStatusEntity.builder()
                        .status(status)
                        .round(round)
                        .judge(au)
                        .build();
                judgeRoundStatusEntities.add(judgeRoundStatus);
                judgeStatusCount++;
            }
        }
        judgeRoundStatusRepository.saveAll(judgeRoundStatusEntities);
        judgeRoundStatusService.invalidateForRound(round.getId());
        log.debug("Инвалидирован кэш статуса захода roundId={}", round.getId());
        log.info("Создано {} статусов судей для захода={}", judgeStatusCount, round.getId());
    }
}
