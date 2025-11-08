package org.bn.sensation.core.judgemilestoneresult.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilestoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.judgemilestoneresult.service.mapper.JudgeMilestoneResultDtoMapper;
import org.bn.sensation.core.judgemilestoneresult.service.mapper.JudgeMilestoneResultRoundRequestMapper;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.RoundStateMachineService;
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
public class JudgeMilestoneResultServiceImpl implements JudgeMilestoneResultService {

    private final CurrentUser currentUser;
    private final JudgeMilestoneResultDtoMapper judgeMilestoneResultDtoMapper;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final JudgeMilestoneResultRoundRequestMapper judgeMilestoneResultRoundRequestMapper;
    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;
    private final RoundStateMachineService roundStateMachineService;

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
    public JudgeMilestoneResultDto createOrUpdate(JudgeMilestoneResultRoundRequest request, Long userId) {
        log.info("Создание или обновление результата судьи для раунда={}, участника={}, судьи={}",
                request.getRoundId(), request.getParticipantId(), userId);
        JudgeMilestoneResultEntity entity;
        if (request.getId() == null) {
            log.debug("Создание нового результата судьи");
            Preconditions.checkArgument(userId != null, "ID судьи не может быть null");
            RoundEntity round = roundRepository.getByIdFullOrThrow(request.getRoundId());
            ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                    round.getMilestone().getActivity(), userId, ua -> ua.getUser().getId().equals(userId));
            log.debug("Найден раунд={} и судья={} для создания", round.getId(), activityUser.getId());
            entity = createEntity(request, round, activityUser);
        } else {
            log.debug("Обновление существующего результата судьи с id={}", request.getId());
            entity = judgeMilestoneResultRepository.getByIdWithUserAndMilestoneOrThrow(request.getId());
            judgeMilestoneResultRoundRequestMapper.updateJudgeMilestoneResultRoundFromRequest(request, entity);
        }
        validateScores(entity.getRound().getMilestone(), List.of(entity));
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);
        log.info("Успешно создан или обновлен результат судьи с id={} для участника={}, критерия={}, оценки={}",
                saved.getId(), request.getParticipantId(), request.getMilestoneCriterionId(), request.getScore());
        judgeMilestoneStatusCacheService.invalidateForMilestone(saved.getRound().getMilestone().getId());
        log.debug("Инвалидирован кэш статуса этапа milestoneId={} после изменения статуса судьи={}",
                saved.getRound().getMilestone().getId(), saved.getActivityUser().getId());
        return judgeMilestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<JudgeMilestoneResultDto> createOrUpdateForRound(Long roundId, List<JudgeMilestoneResultRoundRequest> requests) {
        log.info("Создание или обновление результатов судьи для раунда={}, количество запросов={}",
                roundId, requests != null ? requests.size() : 0);

        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
//        Preconditions.checkArgument(requests != null && !requests.isEmpty(), "Отсутствуют результаты оценки участника");
        RoundEntity round = roundRepository.getByIdFullOrThrow(roundId);
        Preconditions.checkState(round.getState() == RoundState.IN_PROGRESS || round.getState() == RoundState.READY,
                "Результаты не могут быть сохранены, т.к. раунд в состоянии %s", round.getState().name());

        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                round.getMilestone().getActivity(), userId,
                ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId())
                        && ua.getPosition().isJudge());

        log.debug("Найден судья={} для раунда={}, сторона={}",
                activityUser.getId(), roundId, activityUser.getPartnerSide());

        long resultsCount = validateResultsCount(roundId, requests, round, activityUser);
        List<JudgeMilestoneResultDto> dtos = new ArrayList<>();
        if (resultsCount > 0) {
            List<JudgeMilestoneResultEntity> toSave = new ArrayList<>();
            log.debug("Обработка {} запросов для раунда {}", requests.size(), roundId);
            requests.forEach(request -> {
                if (request.getId() == null) {
                    log.debug("Создание нового результата для участника={}, критерия={}",
                            request.getParticipantId(), request.getMilestoneCriterionId());
                    toSave.add(createEntity(request, round, activityUser));
                } else {
                    log.debug("Обновление существующего результата с id={}", request.getId());
                    JudgeMilestoneResultEntity entity = judgeMilestoneResultRepository.getByIdWithUserAndMilestoneOrThrow(request.getId());
                    Preconditions.checkArgument(entity.getActivityUser().getUser().getId().equals(currentUser.getSecurityUser().getId()),
                            "Нельзя изменить результат другого судьи");
                    judgeMilestoneResultRoundRequestMapper.updateJudgeMilestoneResultRoundFromRequest(request, entity);
                    toSave.add(entity);
                }
            });

            validateScores(round.getMilestone(), toSave);

            dtos.addAll(judgeMilestoneResultRepository.saveAll(toSave)
                    .stream().map(judgeMilestoneResultDtoMapper::toDto).toList());
        }

        log.info("Изменение статуса раунда судьи на READY для судьи={}, раунда={}", activityUser.getId(), roundId);
        JudgeRoundStatusEntity status = judgeRoundStatusRepository.findByRoundIdAndJudgeId(
                        round.getId(), activityUser.getId())
                .orElse(JudgeRoundStatusEntity.builder().round(round).judge(activityUser).build());
        status.setStatus(JudgeRoundStatus.READY);
        judgeRoundStatusRepository.save(status);
        log.debug("Попытка пометить раунд {} как READY", roundId);
        roundStateMachineService.sendEvent(round, RoundEvent.MARK_READY);

        judgeMilestoneStatusCacheService.invalidateForMilestone(round.getMilestone().getId());
        log.debug("Инвалидирован кэш статуса этапа milestoneId={} после изменения статуса судьи", round.getMilestone().getId());

        log.info("Успешно обработано {} результатов для раунда {}", dtos.size(), roundId);
        return dtos;
    }

    private long validateResultsCount(Long roundId, List<JudgeMilestoneResultRoundRequest> requests, RoundEntity round, ActivityUserEntity activityUser) {
        log.debug("Проверка количества результатов для раунда={}, судья={}, сторона судьи={}", roundId, activityUser.getId(), activityUser.getPartnerSide());

        List<JudgeMilestoneResultEntity> resultsByRound = judgeMilestoneResultRepository.findByRoundIdAndActivityUserId(roundId, activityUser.getId());
        long toCreate = requests.stream().filter(request -> request.getId() == null).count();
        log.debug("Результаты проверки: существующих={}, к созданию={}", resultsByRound.size(), toCreate);
        long resultsTotalCount = activityUser.getPartnerSide() != null
                ? countForPartnerSide(activityUser.getPartnerSide(), round)
                : countForPartnerSide(PartnerSide.LEADER, round) + countForPartnerSide(PartnerSide.FOLLOWER, round);
        Preconditions.checkArgument(resultsByRound.size() + toCreate == resultsTotalCount,
                "Не соответствует количество результатов проверки. существующих=%s, к созданию=%s, всего требуется=%s",
                resultsByRound.size(), toCreate, resultsTotalCount);
        return resultsTotalCount;
    }

    private long countForPartnerSide(PartnerSide partnerSide, RoundEntity round) {
        long participantsCount = round.getParticipants().stream().filter(p -> p.getPartnerSide() == partnerSide).count();
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
                request.getRoundId(), request.getParticipantId(), request.getMilestoneCriterionId(), request.getScore());

        Preconditions.checkArgument(roundEntity.getId().equals(request.getRoundId()),
                "ID раунда не может быть null или отличаться от целевого раунда");
        Preconditions.checkArgument(request.getParticipantId() != null, "ID участника не может быть null");
        Preconditions.checkArgument(request.getMilestoneCriterionId() != null, "ID критерия не может быть null");

        ParticipantEntity participant = roundEntity.getParticipants().stream()
                .filter(p -> p.getId().equals(request.getParticipantId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Участник не участвует в данном раунде"));
        MilestoneCriterionEntity milestoneCriterion = roundEntity.getMilestone().getMilestoneRule().getMilestoneCriteria().stream()
                .filter(ca -> ca.getId().equals(request.getMilestoneCriterionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("В данном раунде нет такого критерия: " + request.getMilestoneCriterionId()));

        log.debug("Найден участник={} и критерий={} для создания результата", participant.getId(), milestoneCriterion.getId());

        Preconditions.checkArgument(activityUser.getPosition().isJudge(), "Оценивающий должен быть судьей");

        //TODO тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
        Preconditions.checkArgument(milestoneCriterion.getPartnerSide() == null
                        || activityUser.getPartnerSide() == null
                        || Objects.equals(milestoneCriterion.getPartnerSide(), activityUser.getPartnerSide()),
                "Сторона судьи и критерия не совпадает");
        boolean exists = judgeMilestoneResultRepository.existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriterionId(
                request.getRoundId(), request.getParticipantId(), activityUser.getId(), request.getMilestoneCriterionId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        JudgeMilestoneResultEntity entity = judgeMilestoneResultRoundRequestMapper.toEntity(request);
        entity.setParticipant(participant);
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
    public List<JudgeMilestoneResultDto> findByParticipantId(Long participantId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByParticipantId(participantId);
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

    @Override
    @Transactional(readOnly = true)
    public Page<JudgeMilestoneResultDto> findAll(Pageable pageable) {
        return judgeMilestoneResultRepository.findAll(pageable).map(judgeMilestoneResultDtoMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!judgeMilestoneResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Результат раунда не найден с id: " + id);
        }

        judgeMilestoneResultRepository.deleteById(id);
    }

    private void validateScores(MilestoneEntity milestone, List<JudgeMilestoneResultEntity> resultsToSave) {
        switch (milestone.getMilestoneRule().getAssessmentMode()) {
            case SCORE -> validateScoreMode(resultsToSave);
            case PASS -> validatePassMode(resultsToSave);
            case PLACE -> validatePlaceMode(milestone, resultsToSave);
        }
    }

    private void validateScoreMode(List<JudgeMilestoneResultEntity> resultsToSave) {
        log.debug("Проверка режима SCORE для {} запросов раунда", resultsToSave.size());
        for (JudgeMilestoneResultEntity result : resultsToSave) {
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

    private void validatePassMode(List<JudgeMilestoneResultEntity> resultsToSave) {
        for (JudgeMilestoneResultEntity result : resultsToSave) {
            Preconditions.checkArgument(result.getScore() != null,
                    "Оценка не может быть null для режима PASS");
            Preconditions.checkArgument(result.getScore().intValue() == 0 || result.getScore().intValue() == 1,
                    "Для режима PASS оценка может быть только 0 или 1, получена: %s", result.getScore());
        }
    }

    private void validatePlaceMode(MilestoneEntity milestone, List<JudgeMilestoneResultEntity> resultsToSave) {
        for (PartnerSide partnerSide : PartnerSide.values()) {
            long countInCurrentMilestone = participantRepository.countByMilestones_IdAndPartnerSide(milestone.getId(), partnerSide);
            List<JudgeMilestoneResultEntity> partnerSideResults = resultsToSave.stream()
                    .filter(req -> req.getParticipant().getPartnerSide() == partnerSide)
                    .toList();
            // Проверяем, что все оценки в диапазоне от 1 до количества участников
            for (JudgeMilestoneResultEntity result : partnerSideResults) {
                Preconditions.checkArgument(result.getScore() != null,
                        "Оценка не может быть null для режима PLACE");
                Preconditions.checkArgument(result.getScore().intValue() >= 1
                                && result.getScore().intValue() <= countInCurrentMilestone,
                        "Для режима PLACE оценка должна быть от 1 до %s, получена: %s",
                        countInCurrentMilestone, result.getScore());
            }

            // Проверяем уникальность мест (каждое место может быть только один раз)
            List<Integer> scores = partnerSideResults.stream()
                    .map(JudgeMilestoneResultEntity::getScore)
                    .sorted()
                    .toList();

            long uniqueScoresCount = scores.stream().distinct().count();
            Preconditions.checkArgument(uniqueScoresCount == scores.size(),
                    "В режиме PLACE каждое место должно быть уникальным. Найдены дублирующиеся места: %s", scores);
        }
    }

    @Override
    public JudgeMilestoneResultDto create(JudgeMilestoneResultRoundRequest request) {
        return createOrUpdate(request, currentUser.getSecurityUser().getId());
    }

    @Override
    public JudgeMilestoneResultDto update(Long id, JudgeMilestoneResultRoundRequest request) {
        request.setId(id);
        return createOrUpdate(request, currentUser.getSecurityUser().getId());
    }

/*    private void validateByAssessmentMode(
            MilestoneEntity milestone,
            List<JudgeMilestoneResultRoundRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        switch (milestone.getMilestoneRule().getAssessmentMode()) {
            case SCORE -> validateScoreMode(requests, results);
            case PASS -> validatePassMode(milestone, requests, results);
            case PLACE -> validatePlaceMode(milestone, requests, results);
        }
    }

    private void validateScoreMode(
            List<JudgeMilestoneResultRoundRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        log.debug("Проверка режима SCORE для {} запросов", requests.size());

        for (JudgeMilestoneResultRoundRequest request : requests) {
            JudgeMilestoneResultEntity result = results.get(request.getId());
            MilestoneCriterionEntity milestoneCriterion = result.getMilestoneCriterion();

            log.debug("Проверка оценки={} для критерия={} со шкалой={}",
                    request.getScore(), milestoneCriterion.getCriterion().getName(), milestoneCriterion.getScale());

            Preconditions.checkArgument(request.getScore() != null,
                    "Оценка не может быть null для режима SCORE");
            Preconditions.checkArgument(request.getScore() > 0,
                    "Оценка не может быть отрицательной для режима SCORE");
            Preconditions.checkArgument(request.getScore() <= milestoneCriterion.getScale(),
                    "Оценка %s не может превышать максимальную шкалу %s для критерия %s",
                    request.getScore(), milestoneCriterion.getScale(), milestoneCriterion.getCriterion().getName());
        }

        log.debug("Проверка режима SCORE завершена успешно");
    }

    private void validatePassMode(
            MilestoneEntity milestone,
            List<JudgeMilestoneResultRoundRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        for (JudgeMilestoneResultRoundRequest request : requests) {
            Preconditions.checkArgument(request.getScore() != null,
                    "Оценка не может быть null для режима PASS");
            Preconditions.checkArgument(request.getScore() == 0 || request.getScore() == 1,
                    "Для режима PASS оценка может быть только 0 или 1, получена: %s", request.getScore());
        }

        if (!milestone.getMilestoneOrder().equals(0) && milestone.getMilestoneRule().getStrictPassMode()) {
            // Получаем количество участников следующего этапа
            int nextMilestoneParticipantLimit = milestoneRepository.getParticipantLimitForNextMilestone(milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1);

            for (PartnerSide partnerSide : PartnerSide.values()) {
                long countInCurrentMilestone = milestone.getRounds().stream()
                        .flatMap(round -> round.getParticipants().stream())
                        .filter(p -> p.getPartnerSide() == partnerSide).count();

                // Максимальное количество участников, которые могут пройти (оценка = 1)
                long realPassCount = Math.min(nextMilestoneParticipantLimit, countInCurrentMilestone);
                long passCount = requests.stream()
                        .filter(req -> results.get(req.getId()).getParticipant().getPartnerSide() == partnerSide
                                && req.getScore().equals(1))
                        .count();
                Preconditions.checkArgument(realPassCount == passCount,
                        "Количество прошедших участников должно быть %d", realPassCount);
            }
        }
    }

    private void validatePlaceMode(
            MilestoneEntity milestone,
            List<JudgeMilestoneResultRoundRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        for (PartnerSide partnerSide : PartnerSide.values()) {
            long countInCurrentMilestone = milestone.getRounds().stream()
                    .flatMap(round -> round.getParticipants().stream())
                    .filter(p -> p.getPartnerSide() == partnerSide).count();
            List<JudgeMilestoneResultRoundRequest> partnerSideRequests = requests.stream()
                    .filter(req -> results.get(req.getId()).getParticipant().getPartnerSide() == partnerSide)
                    .toList();
            // Проверяем, что все оценки в диапазоне от 1 до количества участников
            for (JudgeMilestoneResultRoundRequest request : partnerSideRequests) {
                Preconditions.checkArgument(request.getScore() != null,
                        "Оценка не может быть null для режима PLACE");
                Preconditions.checkArgument(request.getScore() >= 1 && request.getScore() <= countInCurrentMilestone,
                        "Для режима PLACE оценка должна быть от 1 до %s, получена: %s",
                        countInCurrentMilestone, request.getScore());
            }

            // Проверяем уникальность мест (каждое место может быть только один раз)
            List<Integer> scores = partnerSideRequests.stream()
                    .map(JudgeMilestoneResultRoundRequest::getScore)
                    .sorted()
                    .toList();

            long uniqueScoresCount = scores.stream().distinct().count();
            Preconditions.checkArgument(uniqueScoresCount == scores.size(),
                    "В режиме PLACE каждое место должно быть уникальным. Найдены дублирующиеся места: %s", scores);
        }
    }*/
}
