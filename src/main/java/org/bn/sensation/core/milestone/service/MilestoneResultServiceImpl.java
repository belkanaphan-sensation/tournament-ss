package org.bn.sensation.core.milestone.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatusEntity;
import org.bn.sensation.core.judge.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.judge.repository.JudgeMilestoneStatusRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.entity.PassStatus;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneResultRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneResultRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneResultRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneResultDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneResultRequestMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneResultServiceImpl implements MilestoneResultService {

    private static final int FINAL_RESULT_LIMIT = 3;
    private final JudgeMilestoneStatusRepository judgeMilestoneStatusRepository;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    private final CreateMilestoneResultRequestMapper createMilestoneResultRequestMapper;
    private final UpdateMilestoneResultRequestMapper updateMilestoneResultRequestMapper;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public BaseRepository<MilestoneResultEntity> getRepository() {
        return milestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> getMapper() {
        return milestoneResultDtoMapper;
    }

    @Override
    @Transactional
    public List<MilestoneResultDto> calculateResults(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + milestoneId));
        Map<Long, JudgeMilestoneStatusEntity> judgeResults = judgeMilestoneStatusRepository.findByMilestoneId(milestoneId)
                .stream()
                .collect(Collectors.toMap(jm -> jm.getJudge().getId(), Function.identity()));
        Preconditions.checkState(milestone.getActivity().getUserAssignments().stream()
                        .allMatch(ua -> judgeResults.get(ua.getId()) != null
                                && judgeResults.get(ua.getId()).getStatus() == JudgeMilestoneStatus.READY),
                "Для получения результатов этапа все судьи должны завершить этап");
        if (milestone.getResults().isEmpty()) {
            return calculate(milestone);
        } else {
            //пока не реализовано
            return List.of();
        }
    }

    private List<MilestoneResultDto> calculate(MilestoneEntity milestone) {
        //Map<ParticipantId, Map<RoundId, List<JudgeMilestoneResultEntity>>>
        Map<Long, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap =
            judgeMilestoneResultRepository.findByMilestoneId(milestone.getId())
                .stream()
                .collect(Collectors.groupingBy(
                    jm -> jm.getParticipant().getId(),
                    Collectors.groupingBy(jmr -> jmr.getRound().getId())
                ));

        List<MilestoneResultEntity> resultEntities = milestone.getRounds()
            .stream()
            .filter(round -> !round.getExtraRound())
            .flatMap(round -> round.getParticipants().stream()
                .map(participant -> createMilestoneResult(participant, milestone, round, resultsMap)))
            .toList();

        Integer limit = getParticipantLimit(milestone);

        Map<Double, List<MilestoneResultEntity>> groupedByScore = resultEntities.stream()
            .collect(Collectors.groupingBy(MilestoneResultEntity::getTotalScore));

        List<Double> sortedScores = groupedByScore.keySet().stream()
            .sorted(getScoreComparator(milestone))
            .toList();

        distributePlaces(groupedByScore, sortedScores, limit);

        List<MilestoneResultEntity> savedEntities = milestoneResultRepository.saveAll(resultEntities);

        return savedEntities.stream()
            .map(milestoneResultDtoMapper::toDto)
            .toList();
    }

    private MilestoneResultEntity createMilestoneResult(
            ParticipantEntity participant,
            MilestoneEntity milestone,
            RoundEntity round,
            Map<Long, Map<Long, List<JudgeMilestoneResultEntity>>> resultsMap) {

        MilestoneResultEntity resultEntity = new MilestoneResultEntity();
        resultEntity.setParticipant(participant);
        resultEntity.setMilestone(milestone);
        resultEntity.setJudgePassed(PassStatus.FAILED);
        resultEntity.setFinallyApproved(false);

        List<JudgeMilestoneResultEntity> participantResults = resultsMap
            .getOrDefault(participant.getId(), Collections.emptyMap())
            .get(round.getId());

        Preconditions.checkArgument(participantResults != null && !participantResults.isEmpty(),
            "Отсутствуют результаты для участника " + participant.getId());

        double totalScore = participantResults.stream()
            .mapToDouble(pr -> pr.getMilestoneCriteria().getWeight()
                .multiply(new BigDecimal(pr.getScore())).doubleValue())
            .sum();

        resultEntity.setTotalScore(totalScore);
        return resultEntity;
    }

    private Integer getParticipantLimit(MilestoneEntity milestone) {
        return milestone.getMilestoneOrder().compareTo(0) > 0
            ? milestoneRepository.getParticipantLimitForNextMilestone(
                milestone.getActivity().getId(), milestone.getMilestoneOrder() - 1)
            : FINAL_RESULT_LIMIT;
    }

    private Comparator<Double> getScoreComparator(MilestoneEntity milestone) {
        return milestone.getMilestoneRule().getAssessmentMode() != AssessmentMode.PLACE
            ? Comparator.reverseOrder()
            : Comparator.naturalOrder();
    }

    private void distributePlaces(
            Map<Double, List<MilestoneResultEntity>> groupedByScore,
            List<Double> sortedScores,
            int limit) {

        int remainingSlots = limit;
        for (Double score : sortedScores) {
            List<MilestoneResultEntity> entitiesWithScore = groupedByScore.get(score);
            if (entitiesWithScore.size() <= remainingSlots) {
                entitiesWithScore.forEach(entity -> {
                    entity.setJudgePassed(PassStatus.PASSED);
                    entity.setFinallyApproved(true);
                });
                remainingSlots -= entitiesWithScore.size();
            } else {
                entitiesWithScore.forEach(entity -> {
                    entity.setJudgePassed(PassStatus.PENDING);
                    entity.setFinallyApproved(false);
                });
                break;
            }
        }
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
//        Preconditions.checkArgument(request.getRoundId() != null, "Round ID не может быть null");

        MilestoneEntity milestone = milestoneRepository.findById(request.getMilestoneId())
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));

        ParticipantEntity participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new EntityNotFoundException("Участник не найден с id: " + request.getParticipantId()));

//        RoundEntity round = roundRepository.findById(request.getRoundId())
//                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + request.getRoundId()));

        MilestoneResultEntity result = createMilestoneResultRequestMapper.toEntity(request);
        result.setMilestone(milestone);
        result.setParticipant(participant);
//        result.setRound(round);

        MilestoneResultEntity saved = milestoneResultRepository.save(result);
        return milestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneResultDto update(Long id, UpdateMilestoneResultRequest request) {
        Preconditions.checkArgument(id != null, "ID результата не может быть null");

        MilestoneResultEntity result = milestoneResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат этапа не найден с id: " + id));

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
