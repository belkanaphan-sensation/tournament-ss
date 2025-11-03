package org.bn.sensation.core.milestoneresult.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judgemilestoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.entity.PassStatus;
import org.bn.sensation.core.milestoneresult.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestoneresult.service.dto.CreateMilestoneResultRequest;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
import org.bn.sensation.core.milestoneresult.service.mapper.CreateMilestoneResultRequestMapper;
import org.bn.sensation.core.milestoneresult.service.mapper.MilestoneResultDtoMapper;
import org.bn.sensation.core.milestoneresult.service.mapper.UpdateMilestoneResultRequestMapper;
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

    private static final int FINAL_RESULT_LIMIT = 3;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    private final CreateMilestoneResultRequestMapper createMilestoneResultRequestMapper;
    private final UpdateMilestoneResultRequestMapper updateMilestoneResultRequestMapper;
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
    @Transactional(propagation = Propagation.MANDATORY)
    public List<MilestoneResultDto> calculateResults(@NotNull MilestoneEntity milestone) {
        log.info("Расчет результатов для этапа={}", milestone.getId());
        return calculate(milestone);
    }

    private List<MilestoneResultDto> calculate(MilestoneEntity milestone) {
        log.info("Начинаем расчет результатов для этапа={}, режим оценки={}",
                milestone.getId(), milestone.getMilestoneRule().getAssessmentMode());

        //Map<ParticipantId, Map<RoundId, List<JudgeMilestoneResultEntity>>>
        Map<Long, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap =
                judgeMilestoneResultRepository.findByMilestoneId(milestone.getId())
                        .stream()
                        .filter(jm -> jm.getRound().getState() != RoundState.COMPLETED)
                        .collect(Collectors.groupingBy(
                                jm -> jm.getParticipant().getId(),
                                Collectors.groupingBy(jmr -> jmr.getRound().getId())
                        ));

        log.debug("Сгруппированы результаты судей: {} участников", resultsMap.size());

        List<MilestoneResultEntity> resultEntities = milestone.getRounds()
                .stream()
                .filter(round -> round.getState() != RoundState.COMPLETED)
                .flatMap(round -> round.getParticipants().stream()
                        .map(participant -> createMilestoneResult(participant, milestone, round, resultsMap)))
                .toList();

        log.debug("Создано {} сущностей результатов этапа", resultEntities.size());

        Integer limit = getParticipantLimit(milestone);
        log.debug("Лимит участников для следующего этапа: {}", limit);

        Map<PartnerSide, Map<BigDecimal, List<MilestoneResultEntity>>> groupedByScore = resultEntities.stream()
                .collect(Collectors.groupingBy(res -> res.getParticipant().getPartnerSide(),
                        Collectors.groupingBy(MilestoneResultEntity::getTotalScore)
                ));

        for (Map.Entry<PartnerSide, Map<BigDecimal, List<MilestoneResultEntity>>> entry : groupedByScore.entrySet()) {
            log.debug("Результаты сгруппированы по {} уникальным оценкам для стороны {}", groupedByScore.size(), entry.getKey());
            Map<BigDecimal, List<MilestoneResultEntity>> groupedForPartnerSide = entry.getValue();
            List<BigDecimal> sortedScores = groupedForPartnerSide.keySet().stream()
                    .sorted(getScoreComparator(milestone))
                    .toList();
            log.debug("Оценки отсортированы: {}", sortedScores);

            long placesRemained = limit - milestone.getResults().stream()
                    .filter(res -> res.getParticipant().getPartnerSide() == entry.getKey()
                            && res.getJudgePassed() == PassStatus.PASSED)
                    .count();
            distributePlaces(groupedForPartnerSide, sortedScores, placesRemained);
        }

        List<MilestoneResultEntity> savedEntities = milestoneResultRepository.saveAll(resultEntities);
        log.info("Сохранено {} результатов этапа в базу данных", savedEntities.size());
        milestone.getResults().addAll(savedEntities);

        return milestone.getResults().stream()
                .map(milestoneResultDtoMapper::toDto)
                .toList();
    }

    private MilestoneResultEntity createMilestoneResult(
            ParticipantEntity participant,
            MilestoneEntity milestone,
            RoundEntity round,
            Map<Long, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap) {

        log.debug("Создание результата этапа для участника={}, раунда={}", participant.getId(), round.getId());

        MilestoneResultEntity resultEntity = MilestoneResultEntity.builder()
                .participant(participant)
                .milestone(milestone)
                .round(round)
                .judgePassed(PassStatus.FAILED)
                .finallyApproved(false)
                .build();

        List<JudgeMilestoneResultEntity> participantResults = resultsMap
                .getOrDefault(participant.getId(), Collections.emptyMap())
                .get(round.getId());

        Preconditions.checkArgument(participantResults != null && !participantResults.isEmpty(),
                "Отсутствуют результаты для участника " + participant.getId());

        log.debug("Найдено {} результатов судей для участника={} в раунде={}",
                participantResults.size(), participant.getId(), round.getId());

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

        log.debug("Итоговая оценка для участника={}: {}", participant.getId(), totalScore);
        resultEntity.setTotalScore(totalScore);
        return resultEntity;
    }

    private Integer getParticipantLimit(MilestoneEntity milestone) {
        return milestone.getMilestoneOrder().compareTo(0) > 0
                ? milestoneRepository.getParticipantLimitForNextMilestone(
                milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1)
                : FINAL_RESULT_LIMIT;
    }

    private Comparator<BigDecimal> getScoreComparator(MilestoneEntity milestone) {
        return milestone.getMilestoneRule().getAssessmentMode() != AssessmentMode.PLACE
                ? Comparator.reverseOrder()
                : Comparator.naturalOrder();
    }

    private void distributePlaces(
            Map<BigDecimal, List<MilestoneResultEntity>> groupedByScore,
            List<BigDecimal> sortedScores,
            long limit) {

        log.debug("Распределение мест: лимит={}, количество уникальных оценок={}", limit, sortedScores.size());

        long remainingSlots = limit;
        for (BigDecimal score : sortedScores) {
            List<MilestoneResultEntity> entitiesWithScore = groupedByScore.get(score);
            log.debug("Обработка оценки={}, участников с этой оценкой={}, оставшихся мест={}",
                    score, entitiesWithScore.size(), remainingSlots);

            if (entitiesWithScore.size() <= remainingSlots) {
                log.debug("Все участники с оценкой={} проходят в следующий этап", score);
                entitiesWithScore.forEach(entity -> {
                    entity.setJudgePassed(PassStatus.PASSED);
                    entity.setFinallyApproved(true);
                });
                remainingSlots -= entitiesWithScore.size();
            } else {
                log.debug("Участники с оценкой={} получают статус PENDING (не все проходят)", score);
                entitiesWithScore.forEach(entity -> {
                    entity.setJudgePassed(PassStatus.PENDING);
                    entity.setFinallyApproved(false);
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

        MilestoneResultEntity result = createMilestoneResultRequestMapper.toEntity(request);
        result.setMilestone(milestone);
        result.setParticipant(participant);
        result.setRound(round);

        MilestoneResultEntity saved = milestoneResultRepository.save(result);
        return milestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneResultDto update(Long id, UpdateMilestoneResultRequest request) {
        Preconditions.checkArgument(id != null, "ID результата не может быть null");

        MilestoneResultEntity result = milestoneResultRepository.getByIdOrThrow(id);

        updateMilestoneResultRequestMapper.updateMilestoneResultFromRequest(request, result);

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
