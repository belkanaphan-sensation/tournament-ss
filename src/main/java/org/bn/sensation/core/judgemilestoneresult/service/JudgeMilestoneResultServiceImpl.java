package org.bn.sensation.core.judgemilestoneresult.service;

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
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.judgemilestoneresult.service.mapper.JudgeMilestoneResultDtoMapper;
import org.bn.sensation.core.judgemilestoneresult.service.mapper.JudgeMilestoneResultRoundRequestMapper;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.judgeroundstatus.service.JudgeRoundStatusService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeMilestoneResultServiceImpl implements JudgeMilestoneResultService {

    private final CurrentUser currentUser;
    private final JudgeMilestoneResultDtoMapper judgeMilestoneResultDtoMapper;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final JudgeMilestoneResultRoundRequestMapper judgeMilestoneResultRoundRequestMapper;
    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final JudgeRoundStatusService judgeRoundStatusService;
    private final MilestoneRepository milestoneRepository;
    private final RoundRepository roundRepository;
    private final ContestantRepository contestantRepository;

    @Override
    public BaseRepository<JudgeMilestoneResultEntity> getRepository() {
        return judgeMilestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultDto> getMapper() {
        return judgeMilestoneResultDtoMapper;
    }

    @Override
    @Transactional
    public List<JudgeMilestoneResultDto> createOrUpdateForRound(Long roundId, List<JudgeMilestoneResultRoundRequest> requests) {
        log.info("Создание или обновление результатов судьи для раунда={}, количество запросов={}",
                roundId, requests != null ? requests.size() : 0);
        RoundEntity round = roundRepository.getByIdFullOrThrow(roundId);
        Preconditions.checkState(round.getMilestone().getState() == MilestoneState.IN_PROGRESS || round.getMilestone().getState() == MilestoneState.SUMMARIZING,
                "Результаты не могут быть сохранены, т.к. этап в состоянии %s", round.getMilestone().getState().name());

        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                round.getMilestone().getActivity(), userId,
                ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId())
                        && ua.getPosition().isJudge());

        log.debug("Найден судья={} для раунда={}, сторона={}",
                activityUser.getId(), roundId, activityUser.getPartnerSide());

        Map<Long, JudgeMilestoneResultEntity> resultsByRound = judgeMilestoneResultRepository.findByRoundIdAndActivityUserId(roundId, activityUser.getId())
                .stream().collect(Collectors.toMap(JudgeMilestoneResultEntity::getId, Function.identity()));
        long resultsCount = validateResultsCount(resultsByRound.size(), requests, round, activityUser);
        List<JudgeMilestoneResultDto> dtos = new ArrayList<>();
        if (resultsCount > 0) {
            List<JudgeMilestoneResultEntity> toCreate = new ArrayList<>();
            List<JudgeMilestoneResultEntity> toSave = new ArrayList<>();
            log.debug("Обработка {} запросов для раунда {}", requests.size(), roundId);
            requests.forEach(request -> {
                if (request.getId() == null) {
                    log.debug("Создание нового результата для участника={}, критерия={}",
                            request.getContestantId(), request.getMilestoneCriterionId());
                    toCreate.add(createEntity(request, round, activityUser));
                } else {
                    log.debug("Обновление существующего результата с id={}", request.getId());
                    JudgeMilestoneResultEntity entity = resultsByRound.get(request.getId());
                    Preconditions.checkArgument(entity != null,
                            "Результат с id={} не найден", request.getId());
                    judgeMilestoneResultRoundRequestMapper.updateJudgeMilestoneResultRoundFromRequest(request, entity);
                    toSave.add(entity);
                }
            });

            validateScores(round.getMilestone(), activityUser, toCreate, resultsByRound.values());

            toSave.addAll(toCreate);
            dtos.addAll(judgeMilestoneResultRepository.saveAll(toSave)
                    .stream().map(judgeMilestoneResultDtoMapper::toDto).toList());
        }

        log.info("Изменение статуса раунда судьи на READY для судьи={}, раунда={}", activityUser.getId(), roundId);
        JudgeRoundStatusEntity status = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                        round.getId(), activityUser.getId())
                .orElse(JudgeRoundStatusEntity.builder().round(round).judge(activityUser).build());
        status.setStatus(JudgeRoundStatus.READY);
        judgeRoundStatusRepository.save(status);

        judgeRoundStatusService.invalidateForRound(round.getId());
        log.debug("Инвалидирован кэш статуса раунда roundId={}", round.getId());
        judgeMilestoneStatusCacheService.invalidateForMilestone(round.getMilestone().getId());
        log.debug("Инвалидирован кэш статуса этапа milestoneId={} после изменения статуса судьи", round.getMilestone().getId());

        log.info("Успешно обработано {} результатов для раунда {}", dtos.size(), roundId);
        return dtos;
    }

    private long validateResultsCount(
            int resultsByRoundSize,
            List<JudgeMilestoneResultRoundRequest> requests,
            RoundEntity round,
            ActivityUserEntity activityUser) {
        log.debug("Проверка количества результатов для раунда={}, судья={}, сторона судьи={}", round.getId(), activityUser.getId(), activityUser.getPartnerSide());
        long toCreate = requests.stream().filter(request -> request.getId() == null).count();
        log.debug("Результаты проверки: существующих={}, к созданию={}", resultsByRoundSize, toCreate);
        long resultsTotalCount = !round.getMilestone().getMilestoneRule().getContestantType().hasPartnerSide()
                ? round.getContestants().size() * round.getMilestone().getMilestoneRule().getMilestoneCriteria().size()
                : activityUser.getPartnerSide() != null
                ? countForPartnerSide(activityUser.getPartnerSide(), round)
                : countForPartnerSide(PartnerSide.LEADER, round) + countForPartnerSide(PartnerSide.FOLLOWER, round);
        Preconditions.checkArgument(resultsByRoundSize + toCreate == resultsTotalCount,
                "Не соответствует количество результатов проверки. существующих=%s, к созданию=%s, всего требуется=%s",
                resultsByRoundSize, toCreate, resultsTotalCount);
        return resultsTotalCount;
    }

    private long countForPartnerSide(PartnerSide partnerSide, RoundEntity round) {
        long participantsCount = round.getContestants().stream()
                .filter(c -> c.getParticipants().iterator().next().getPartnerSide() == partnerSide)
                .count();
        long criteriaCount = round.getMilestone().getMilestoneRule().getMilestoneCriteria().stream()
                .filter(ca -> ca.getPartnerSide() == null || ca.getPartnerSide() == partnerSide)
                .count();
        long resultsTotalCount = participantsCount * criteriaCount;
        log.debug("Результаты проверки для стороны: сторона={}, участников={}, критериев={}, всего={}",
                partnerSide, participantsCount, criteriaCount, resultsTotalCount);
        return resultsTotalCount;
    }

    //TODO тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
    private JudgeMilestoneResultEntity createEntity(JudgeMilestoneResultRoundRequest request, RoundEntity roundEntity, ActivityUserEntity activityUser) {
        log.debug("Создание сущности результата судьи для раунда={}, участника={}, критерия={}, оценки={}",
                request.getRoundId(), request.getContestantId(), request.getMilestoneCriterionId(), request.getScore());

        Preconditions.checkArgument(roundEntity.getId().equals(request.getRoundId()),
                "ID раунда не может быть null или отличаться от целевого раунда");
        Preconditions.checkArgument(request.getContestantId() != null, "ID участника не может быть null");
        Preconditions.checkArgument(request.getMilestoneCriterionId() != null, "ID критерия не может быть null");
        Preconditions.checkArgument(activityUser.getPosition().isJudge(), "Оценивающий должен быть судьей");

        MilestoneCriterionEntity milestoneCriterion = roundEntity.getMilestone().getMilestoneRule().getMilestoneCriteria().stream()
                .filter(ca -> ca.getId().equals(request.getMilestoneCriterionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("В данном раунде нет такого критерия: " + request.getMilestoneCriterionId()));

        ContestantEntity contestant = contestantRepository.getByIdOrThrow(request.getContestantId());
        log.debug("Найден конкурсант={} и критерий={} для создания результата", contestant.getId(), milestoneCriterion.getId());
        Preconditions.checkArgument(roundEntity.getContestants().stream().anyMatch(c -> c.getId().equals(contestant.getId())),
                "Участник не относится к данному раунду");
        //TODO тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
        Preconditions.checkArgument(milestoneCriterion.getPartnerSide() == null
                        || activityUser.getPartnerSide() == null
                        || Objects.equals(milestoneCriterion.getPartnerSide(), activityUser.getPartnerSide()),
                "Сторона судьи и критерия не совпадает");

        boolean exists = judgeMilestoneResultRepository.existsByRoundIdAndContestantIdAndActivityUserIdAndMilestoneCriterionId(
                request.getRoundId(), request.getContestantId(), activityUser.getId(), request.getMilestoneCriterionId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        JudgeMilestoneResultEntity entity = judgeMilestoneResultRoundRequestMapper.toEntity(request);
        entity.setContestant(contestant);
        entity.setRound(roundEntity);
        entity.setActivityUser(activityUser);
        entity.setMilestoneCriterion(milestoneCriterion);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByRoundId(Long roundId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByRoundId(roundId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByMilestoneId(Long milestoneId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByMilestoneId(milestoneId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByRoundIdForCurrentUser(Long roundId) {
        RoundEntity round = roundRepository.getByIdWithUserOrThrow(roundId);
        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                round.getMilestone().getActivity(), userId, ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId()));
        return judgeMilestoneResultRepository.findByRoundIdAndActivityUserId(roundId, activityUser.getId()).stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByMilestoneIdForCurrentUser(Long milestoneId) {
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                milestone.getActivity(), userId, ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId()));

        return judgeMilestoneResultRepository.findByMilestoneIdAndActivityUserId(milestoneId, activityUser.getId()).stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByContestantId(Long participantId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByContestantId(participantId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByActivityUserId(Long activityUserId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByActivityUserId(activityUserId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    private void validateScores(MilestoneEntity milestone, ActivityUserEntity activityUser, List<JudgeMilestoneResultEntity> resultsToCreate, Collection<JudgeMilestoneResultEntity> resultsByRound) {
        List<JudgeMilestoneResultEntity> allResults = new ArrayList<>(resultsByRound);
        allResults.addAll(resultsToCreate);
        switch (milestone.getMilestoneRule().getAssessmentMode()) {
            case SCORE -> validateScoreMode(allResults);
            case PASS -> validatePassMode(milestone, allResults);
            case PLACE -> validatePlaceMode(milestone, activityUser, allResults);
        }
    }

    private void validateScoreMode(List<JudgeMilestoneResultEntity> allResults) {
        log.debug("Проверка режима SCORE для {} запросов раунда", allResults.size());
        for (JudgeMilestoneResultEntity result : allResults) {
            MilestoneCriterionEntity milestoneCriterion = result.getMilestoneCriterion();
            log.debug("Проверка оценки={} для критерия={} со шкалой={}",
                    result.getScore(), milestoneCriterion.getCriterion().getName(), milestoneCriterion.getScale());

            Preconditions.checkArgument(result.getScore() != null,
                    "Оценка не может быть null для режима SCORE");
            Preconditions.checkArgument(result.getScore().intValue() > 0,
                    "Оценка не может быть отрицательной для режима SCORE");
            Preconditions.checkArgument(result.getScore().intValue() <= milestoneCriterion.getScale().intValue(),
                    "Оценка %s не может превышать максимальную шкалу %s для критерия %s",
                    result.getScore(), milestoneCriterion.getScale(), milestoneCriterion.getCriterion().getName());
        }
        log.debug("Проверка режима SCORE завершена успешно");
    }

    private void validatePassMode(MilestoneEntity milestone, List<JudgeMilestoneResultEntity> allResults) {
        for (JudgeMilestoneResultEntity result : allResults) {
            Preconditions.checkArgument(result.getScore() != null,
                    "Оценка не может быть null для режима PASS");
            Preconditions.checkArgument(result.getScore().intValue() == 0 || result.getScore().intValue() == 1,
                    "Для режима PASS оценка может быть только 0 или 1, получена: %s", result.getScore());
        }
        boolean isLastMilestone = milestone.getMilestoneOrder().intValue() == 0;
        boolean isStrictPassMode = milestone.getMilestoneRule().getStrictPassMode();
        if (!isLastMilestone) {
            MilestoneEntity nextMilestone = milestoneRepository.getByActivityIdAndMilestoneOrderOrThrow(milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1);
            isStrictPassMode = nextMilestone.getMilestoneRule().getStrictPassMode();
        }
        if (isStrictPassMode) {
            //сейчас эта проверка корректна т.к. для режима strictPassMode всегда 1 раунд
            int contestantLimit = isLastMilestone
                    ? 1
                    : milestoneRepository.getContestantLimitForNextMilestone(milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1);
            Preconditions.checkArgument(allResults.stream().filter(result -> result.getScore().intValue() == 1).count() == contestantLimit,
                    "Количество прошедших конкурсантов должно быть равно %s".formatted(contestantLimit));
        }
    }

    private void validatePlaceMode(MilestoneEntity milestone, ActivityUserEntity activityUser, List<JudgeMilestoneResultEntity> allResults) {
        if (activityUser.getPartnerSide() == null && milestone.getMilestoneRule().getContestantType().hasPartnerSide()) {
            List<JudgeMilestoneResultEntity> leaders = allResults.stream().filter(res -> res.getContestant().getParticipants().iterator().next().getPartnerSide() == PartnerSide.LEADER).toList();
            validatePlaces(leaders);
            List<JudgeMilestoneResultEntity> followers = allResults.stream().filter(res -> res.getContestant().getParticipants().iterator().next().getPartnerSide() == PartnerSide.FOLLOWER).toList();
            validatePlaces(followers);
        } else {
            validatePlaces(allResults);
        }
    }

    private void validatePlaces(List<JudgeMilestoneResultEntity> results) {
        for (JudgeMilestoneResultEntity result : results) {
            Preconditions.checkArgument(result.getScore() != null,
                    "Оценка не может быть null для режима PLACE");
            Preconditions.checkArgument(result.getScore().intValue() >= 1
                            && result.getScore().intValue() <= results.size(),
                    "Для режима PLACE оценка должна быть от 1 до %s, получена: %s",
                    results.size(), result.getScore());
        }
        // Проверяем уникальность мест (каждое место может быть только один раз)
        List<Integer> scores = results.stream()
                .map(JudgeMilestoneResultEntity::getScore)
                .sorted()
                .toList();
        long uniqueScoresCount = scores.stream().distinct().count();
        Preconditions.checkArgument(uniqueScoresCount == scores.size(),
                "В режиме PLACE каждое место должно быть уникальным. Найдены дублирующиеся места: %s", scores);
    }
}
