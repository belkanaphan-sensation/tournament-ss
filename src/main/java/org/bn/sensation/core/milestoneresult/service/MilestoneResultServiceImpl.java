package org.bn.sensation.core.milestoneresult.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneRoundResultEntity;
import org.bn.sensation.core.milestoneresult.entity.PassStatus;
import org.bn.sensation.core.milestoneresult.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestoneresult.repository.MilestoneRoundResultRepository;
import org.bn.sensation.core.milestoneresult.service.dto.*;
import org.bn.sensation.core.milestoneresult.service.mapper.MilestoneResultDtoMapper;
import org.bn.sensation.core.milestoneresult.service.mapper.MilestoneRoundResultDtoMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneResultServiceImpl implements MilestoneResultService {

    private static final long FINAL_RESULT_LIMIT = 3;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneRoundResultRepository milestoneRoundResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    private final MilestoneRoundResultDtoMapper milestoneRoundResultDtoMapper;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;

    @Override
    public BaseRepository<MilestoneResultEntity> getRepository() {
        return milestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> getMapper() {
        return milestoneResultDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MilestoneResultDto> getByMilestoneId(Long milestoneId) {
        return milestoneResultRepository.findAllByMilestoneId(milestoneId).stream().map(milestoneResultDtoMapper::toDto).toList();
    }

    @Override
    @Transactional
    public List<MilestoneResultDto> acceptResults(Long milestoneId, List<UpdateMilestoneResultRequest> request) {
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        Preconditions.checkState(milestone.getState() == MilestoneState.IN_PROGRESS || milestone.getState() == MilestoneState.SUMMARIZING,
                "Недопустимое состояние этапа %s для обновления результатов".formatted(milestone.getState()));
        return acceptResults(milestone, request).stream().map(milestoneResultDtoMapper::toDto).toList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<MilestoneResultEntity> acceptResults(MilestoneEntity milestone, List<UpdateMilestoneResultRequest> request) {
        log.info("Принятие результатов этапа: milestoneId={}, количество запросов={}", milestone.getId(), request.size());

        Map<Long, MilestoneResultEntity> resultEntityMap = milestone.getResults().stream()
                .collect(Collectors.toMap(MilestoneResultEntity::getId, Function.identity()));
        List<MilestoneResultEntity> toSave = request.stream().map(r -> {
            Preconditions.checkArgument(r.getId() != null,
                    "Требуется ID результата этапа");
            MilestoneResultEntity resultEntity = Optional.ofNullable(resultEntityMap.get(r.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Нет результата с ID: " + r.getId()));
            log.debug("Обновление результата: resultId={}, finallyApproved={}", resultEntity.getId(), r.getFinallyApproved());
            resultEntity.setFinallyApproved(r.getFinallyApproved());
            return resultEntity;
        }).toList();

        if (Boolean.TRUE.equals(milestone.getMilestoneRule().getStrictPassMode())) {
            long approvedCount = milestone.getResults().stream()
                    .filter(res -> res.getFinallyApproved())
                    .count();
            Integer participantLimit = milestone.getMilestoneRule().getParticipantLimit();
            log.debug("Проверка strictPassMode: одобрено участников={}, лимит={}", approvedCount, participantLimit);

            Preconditions.checkArgument(participantLimit.longValue() >= approvedCount,
                    "Количество одобренных участников больше чем требуется");
        }

        milestoneResultRepository.saveAll(toSave);
        log.info("Сохранено {} результатов этапа milestoneId={}", toSave.size(), milestone.getId());

        return milestoneResultRepository.findAllByMilestoneId(milestone.getId());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<MilestoneResultDto> calculateResults(@NotNull MilestoneEntity milestone) {
        if (milestone.getResults().isEmpty()) {
            log.info("Создание основных результатов для этапа={}", milestone.getId());
            Preconditions.checkArgument(milestone.getRounds().stream()
                            .noneMatch(r -> Boolean.TRUE.equals(r.getExtraRound())),
                    "Основные результаты этапа еще не созданы. у этапа не должно быть дополнительных раундов");
            return calculateNewResults(milestone);
        } else {
            log.info("Обновление результатов для этапа по дополнительному раунду={}", milestone.getId());
            List<RoundEntity> extraRounds = milestone.getRounds().stream()
                    .filter(r -> r.getState() == RoundState.OPENED)
                    .filter(r -> Boolean.TRUE.equals(r.getExtraRound()))
                    .toList();
            if (!extraRounds.isEmpty()) {
                Preconditions.checkArgument(extraRounds.size() == 1,
                        "Одновременно может быть только один активный дополнительный раунд");
                return calculateExtraResults(milestone, extraRounds.get(0));
            }
            return milestone.getResults().stream()
                    .map(milestoneResultDtoMapper::toDto)
                    .toList();
        }
    }

    private List<MilestoneResultDto> calculateExtraResults(@NotNull MilestoneEntity milestone, @NotNull RoundEntity round) {
        log.info("Начинаем расчет результатов для дополнительного раунда={} этапа={}, режим оценки={}",
                round.getId(), milestone.getId(), milestone.getMilestoneRule().getAssessmentMode());

        //Map<PartnerSide, Map<ParticipantId, List<JudgeMilestoneResultEntity>>>
        Map<PartnerSide, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap = judgeMilestoneResultRepository.findByRoundId(round.getId())
                .stream().collect(
                        Collectors.groupingBy(jmr -> jmr.getParticipant().getPartnerSide(),
                                Collectors.groupingBy(jmr -> jmr.getParticipant().getId())));
        //Map<ParticipantId, MilestoneResultEntity>
        Map<Long, MilestoneResultEntity> pIdToResult = milestone.getResults().stream()
                .collect(Collectors.toMap(mr -> mr.getParticipant().getId(), Function.identity()));
        long limit = getParticipantLimit(milestone);
        for (Map.Entry<PartnerSide, Map<Long, List<JudgeMilestoneResultEntity>>> entry : resultsMap.entrySet()) {
            log.debug("Результаты сгруппированы по уникальным оценкам для стороны {}", entry.getKey());
            Map<Long, List<JudgeMilestoneResultEntity>> roundJudgeResults = entry.getValue();
            long remainingPlaces = limit - milestone.getResults().stream()
                    .filter(res -> Boolean.TRUE.equals(res.getFinallyApproved())
                            && res.getParticipant().getPartnerSide() ==  entry.getKey()).count();
            List<MilestoneRoundResultEntity> toDistribute = new ArrayList<>();
            List<MilestoneResultEntity> toSave = new ArrayList<>();
            roundJudgeResults.forEach((pId, jmrList) -> {
                MilestoneResultEntity resultEntity = Optional.ofNullable(pIdToResult.get(pId))
                        .orElseThrow(() -> new IllegalArgumentException("У участника %s отсутствует результат этапа по основным раундам".formatted(pId)));
                MilestoneRoundResultEntity roundResult = createMilestoneRoundResult(pId, jmrList, round);
                resultEntity.getRoundResults().add(roundResult);
                roundResult.setMilestoneResult(resultEntity);
                toDistribute.add(roundResult);
                toSave.add(resultEntity);
            });
            Map<BigDecimal, List<MilestoneRoundResultEntity>> groupedForPartnerSide = toDistribute.stream()
                    .collect(Collectors.groupingBy(mrr -> mrr.getTotalScore()));
            List<BigDecimal> sortedScores = groupedForPartnerSide.keySet().stream()
                    .sorted(getScoreComparator(milestone))
                    .toList();
            log.debug("Оценки отсортированы: {}", sortedScores);
            distributePlaces(groupedForPartnerSide, sortedScores, remainingPlaces);

            milestoneResultRepository.saveAll(toSave);
        }
        return milestoneResultRepository.findAllByMilestoneId(milestone.getId()).stream().map(milestoneResultDtoMapper::toDto).toList();
    }

    private List<MilestoneResultDto> calculateNewResults(@NotNull MilestoneEntity milestone) {
        log.info("Начинаем расчет результатов для этапа={}, режим оценки={}",
                milestone.getId(), milestone.getMilestoneRule().getAssessmentMode());

        //Map<ParticipantId, Map<RoundId, List<JudgeMilestoneResultEntity>>>
        Map<Long, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap =
                judgeMilestoneResultRepository.findByMilestoneId(milestone.getId())
                        .stream()
                        .filter(jm -> jm.getRound().getState() == RoundState.OPENED)
                        .collect(Collectors.groupingBy(
                                jm -> jm.getParticipant().getId(),
                                Collectors.groupingBy(jmr -> jmr.getRound().getId())
                        ));

        log.debug("Сгруппированы результаты судей: {} участников", resultsMap.size());

        List<MilestoneResultEntity> createdResults = milestone.getRounds()
                .stream()
                .filter(round -> round.getState() == RoundState.OPENED)
                .flatMap(round -> round.getParticipants().stream()
                        .map(participant -> createMilestoneResult(participant, milestone, round, resultsMap)))
                .toList();

        log.debug("Создано {} сущностей результатов этапа", createdResults.size());

        long limit = getParticipantLimit(milestone);
        log.debug("Лимит участников для следующего этапа: {}", limit);

        List<MilestoneRoundResultEntity> allRoundResults = createdResults.stream()
                .flatMap(mr -> mr.getRoundResults().stream())
                .toList();

        Map<PartnerSide, Map<BigDecimal, List<MilestoneRoundResultEntity>>> groupedByScore =
                allRoundResults.stream()
                        .collect(Collectors.groupingBy(
                                mrr -> mrr.getMilestoneResult().getParticipant().getPartnerSide(),
                                Collectors.groupingBy(MilestoneRoundResultEntity::getTotalScore)
                        ));

        for (Map.Entry<PartnerSide, Map<BigDecimal, List<MilestoneRoundResultEntity>>> entry : groupedByScore.entrySet()) {
            log.debug("Результаты сгруппированы по уникальным оценкам для стороны {}", entry.getKey());
            Map<BigDecimal, List<MilestoneRoundResultEntity>> groupedForPartnerSide = entry.getValue();
            List<BigDecimal> sortedScores = groupedForPartnerSide.keySet().stream()
                    .sorted(getScoreComparator(milestone))
                    .toList();
            log.debug("Оценки отсортированы: {}", sortedScores);

            distributePlaces(groupedForPartnerSide, sortedScores, limit);
        }

        List<MilestoneResultEntity> savedEntities = milestoneResultRepository.saveAll(createdResults);
        milestoneRoundResultRepository.saveAll(allRoundResults);
        log.info("Сохранено {} результатов этапа в базу данных", savedEntities.size());
        milestone.getResults().addAll(savedEntities);

        return savedEntities.stream()
                .map(milestoneResultDtoMapper::toDto)
                .toList();
    }

    private MilestoneRoundResultEntity createMilestoneRoundResult(Long participantId, List<JudgeMilestoneResultEntity> participantResults, RoundEntity round) {
        log.debug("Создание результата раунда для этапа для участника={}, раунда={}", participantId, round.getId());
        Preconditions.checkArgument(participantResults != null && !participantResults.isEmpty(),
                "Отсутствуют результаты для участника " + participantId);

        MilestoneRoundResultEntity roundResult = MilestoneRoundResultEntity.builder()
                .round(round)
                .judgePassed(PassStatus.FAILED)
                .totalScore(getTotalScore(participantResults, participantId, round.getId()))
                .build();
        return roundResult;
    }

    private BigDecimal getTotalScore(List<JudgeMilestoneResultEntity> participantResults, Long participantId, Long roundId) {
        log.debug("Найдено {} результатов судей для участника={} в раунде={}",
                participantResults.size(), participantId, roundId);
        BigDecimal totalScore = participantResults.stream()
                .map(pr -> {
                    BigDecimal weight = pr.getMilestoneCriterion().getWeight();
                    BigDecimal score = BigDecimal.valueOf(pr.getScore());
                    BigDecimal weightedScore = weight.multiply(score);

                    log.debug("Расчет взвешенной оценки: критерий={}, вес={}, оценка={}, результат={}",
                            pr.getMilestoneCriterion().getCriterion().getName(),
                            weight,
                            score,
                            weightedScore);

                    return weightedScore;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        log.debug("Итоговая оценка для участника={}: {}", participantId, totalScore);
        return totalScore;
    }

    private MilestoneResultEntity createMilestoneResult(
            ParticipantEntity participant,
            MilestoneEntity milestone,
            RoundEntity round,
            Map<Long, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap) {

        log.debug("Создание результата этапа для участника={}, раунда={}", participant.getId(), round.getId());
        List<JudgeMilestoneResultEntity> participantResults = resultsMap
                .getOrDefault(participant.getId(), Collections.emptyMap())
                .get(round.getId());

        Preconditions.checkArgument(participantResults != null && !participantResults.isEmpty(),
                "Отсутствуют результаты для участника " + participant.getId());

        MilestoneRoundResultEntity roundResult = MilestoneRoundResultEntity.builder()
                .round(round)
                .judgePassed(PassStatus.FAILED)
                .build();
        MilestoneResultEntity resultEntity = MilestoneResultEntity.builder()
                .participant(participant)
                .milestone(milestone)
                .roundResults(new HashSet<>())
                .finallyApproved(false)
                .build();
        resultEntity.getRoundResults().add(roundResult);
        roundResult.setMilestoneResult(resultEntity);
        roundResult.setTotalScore(getTotalScore(participantResults, participant.getId(), round.getId()));
        return resultEntity;
    }

    private long getParticipantLimit(MilestoneEntity milestone) {
        return milestone.getMilestoneOrder().compareTo(0) > 0
                ? milestoneRepository.getParticipantLimitForNextMilestone(
                milestone.getActivity().getId(), milestone.getMilestoneOrder().intValue() - 1).longValue()
                : FINAL_RESULT_LIMIT;
    }

    private Comparator<BigDecimal> getScoreComparator(MilestoneEntity milestone) {
        return milestone.getMilestoneRule().getAssessmentMode() != AssessmentMode.PLACE
                ? Comparator.reverseOrder()
                : Comparator.naturalOrder();
    }

    private void distributePlaces(
            Map<BigDecimal, List<MilestoneRoundResultEntity>> groupedByScore,
            List<BigDecimal> sortedScores,
            long limit) {

        log.debug("Распределение мест: лимит={}, количество уникальных оценок={}", limit, sortedScores.size());

        long remainingSlots = limit;
        for (BigDecimal score : sortedScores) {
            List<MilestoneRoundResultEntity> entitiesWithScore = groupedByScore.get(score);
            log.debug("Обработка оценки={}, участников с этой оценкой={}, оставшихся мест={}",
                    score, entitiesWithScore.size(), remainingSlots);

            if (entitiesWithScore.size() <= remainingSlots) {
                log.debug("Все участники с оценкой={} проходят в следующий этап", score);
                entitiesWithScore.forEach(entity -> {
                    entity.setJudgePassed(PassStatus.PASSED);
                    entity.getMilestoneResult().setFinallyApproved(true);
                });
                remainingSlots -= entitiesWithScore.size();
            } else {
                log.debug("Участники с оценкой={} получают статус PENDING (не все проходят)", score);
                entitiesWithScore.forEach(entity -> {
                    entity.setJudgePassed(PassStatus.PENDING);
                    entity.getMilestoneResult().setFinallyApproved(false);
                });
                break;
            }
        }
        log.debug("Распределение мест завершено, оставшихся мест: {}", remainingSlots);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneResultDto> findAll(Pageable pageable) {
        return milestoneResultRepository.findAll(pageable).map(milestoneResultDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MilestoneResultDto> findById(Long id) {
        return milestoneResultRepository.findById(id)
                .map(milestoneResultDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneResultDto create(CreateMilestoneResultRequest request) {
        Preconditions.checkArgument(request.getMilestoneId() != null, "Milestone ID не может быть null");
        Preconditions.checkArgument(request.getParticipantId() != null, "Participant ID не может быть null");
        Preconditions.checkArgument(request.getRoundId() != null, "Round ID не может быть null");

        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(request.getMilestoneId());
        ParticipantEntity participant = participantRepository.getByIdOrThrow(request.getParticipantId());
        RoundEntity round = roundRepository.getByIdOrThrow(request.getRoundId());


        MilestoneResultEntity result = MilestoneResultEntity.builder()
                .participant(participant)
                .milestone(milestone)
                .finallyApproved(request.getFinallyApproved())
                .build();
        MilestoneResultEntity savedMR = milestoneResultRepository.save(result);
        MilestoneRoundResultEntity roundResult = MilestoneRoundResultEntity.builder()
                .round(round)
                .milestoneResult(savedMR)
                .totalScore(request.getTotalScore())
                .judgePassed(request.getJudgePassed())
                .build();
        MilestoneRoundResultEntity savedMRR = milestoneRoundResultRepository.save(roundResult);
        savedMR.getRoundResults().add(savedMRR);
        return milestoneResultDtoMapper.toDto(savedMR);
    }

    @Override
    @Transactional
    public MilestoneRoundResultDto createMilestoneRoundResult(CreateMilestoneRoundResultRequest request) {
        Preconditions.checkArgument(request.getMilestoneResultId() != null, "Milestone result ID не может быть null");
        Preconditions.checkArgument(request.getRoundId() != null, "Round ID не может быть null");

        MilestoneResultEntity milestoneResult = milestoneResultRepository.getByIdOrThrow(request.getMilestoneResultId());
        RoundEntity round = roundRepository.getByIdOrThrow(request.getRoundId());

        MilestoneRoundResultEntity roundResult = MilestoneRoundResultEntity.builder()
                .round(round)
                .milestoneResult(milestoneResult)
                .totalScore(request.getTotalScore())
                .judgePassed(request.getJudgePassed())
                .build();
        MilestoneRoundResultEntity savedMRR = milestoneRoundResultRepository.save(roundResult);
        return milestoneRoundResultDtoMapper.toDto(savedMRR);
    }

    @Override
    @Transactional
    public MilestoneResultDto update(Long id, UpdateMilestoneResultRequest request) {
        Preconditions.checkArgument(id != null, "ID результата не может быть null");

        MilestoneResultEntity result = milestoneResultRepository.getByIdOrThrow(id);
        result.setFinallyApproved(request.getFinallyApproved());

        MilestoneResultEntity saved = milestoneResultRepository.save(result);
        return milestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Preconditions.checkArgument(id != null, "ID результата не может быть null");
        if (!milestoneResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Результат этапа не найден с id: " + id);
        }
        milestoneResultRepository.deleteById(id);
    }

}
