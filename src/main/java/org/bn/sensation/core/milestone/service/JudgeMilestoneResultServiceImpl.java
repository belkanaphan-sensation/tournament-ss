package org.bn.sensation.core.milestone.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.milestone.service.mapper.JudgeMilestoneResultDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.JudgeMilestoneResultMilestoneRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.JudgeMilestoneResultRoundRequestMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.user.entity.Role;
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
public class JudgeMilestoneResultServiceImpl implements JudgeMilestoneResultService {

    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final JudgeMilestoneResultDtoMapper judgeMilestoneResultDtoMapper;
    private final JudgeMilestoneResultRoundRequestMapper judgeMilestoneResultRoundRequestMapper;
    private final JudgeMilestoneResultMilestoneRequestMapper judgeMilestoneResultMilestoneRequestMapper;
    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final RoundRepository roundRepository;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final CurrentUser currentUser;

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
    public List<JudgeMilestoneResultDto> createOrUpdateForRound(List<JudgeMilestoneResultRoundRequest> requests) {
        List<JudgeMilestoneResultDto> dtos = new ArrayList<>();
        requests.forEach(request -> {
            if (request.getId() == null) {
                dtos.add(create(request));
            } else {
                dtos.add(update(request.getId(), request));
            }
        });
        return dtos;
    }

    @Override
    @Transactional
    public List<JudgeMilestoneResultDto> createOrUpdateForMilestone(Long milestoneId, List<JudgeMilestoneResultMilestoneRequest> requests) {
        MilestoneEntity milestone = milestoneRepository.findByIdWithUserAssignments(milestoneId)
                .orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Данный юзер не зарегистрирован для этапа " + milestoneId));

        int participantsCount = (int) milestone.getRounds().stream()
                .flatMap(round -> round.getParticipants().stream())
                .filter(p -> {
                    if (activityUser.getPartnerSide() != null) {
                        return p.getPartnerSide() == activityUser.getPartnerSide();
                    }
                    return true;
                }).count();
        Preconditions.checkArgument(requests.size() == participantsCount,
                "Отправлены не все результаты этапа");

        Map<Long, JudgeMilestoneResultEntity> results = judgeMilestoneResultRepository.findByIdsIn(
                        requests.stream().map(JudgeMilestoneResultMilestoneRequest::getId).toList())
                .stream()
                .collect(Collectors.toMap(JudgeMilestoneResultEntity::getId, Function.identity()));

        validateByAssessmentMode(milestone, requests, results);

        List<JudgeMilestoneResultEntity> updated = new ArrayList<>();
        requests.forEach(request -> {
            judgeMilestoneResultMilestoneRequestMapper.updateRoundFromRequest(request, results.get(request.getId()));
            updated.add(judgeMilestoneResultRepository.save(results.get(request.getId())));
        });

        return updated.stream().map(judgeMilestoneResultDtoMapper::toDto).toList();
    }

    private void validateByAssessmentMode(
            MilestoneEntity milestone,
            List<JudgeMilestoneResultMilestoneRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        switch (milestone.getMilestoneRule().getAssessmentMode()) {
            case SCORE -> validateScoreMode(requests, results);
            case PASS -> validatePassMode(milestone, requests, results);
            case PLACE -> validatePlaceMode(milestone, requests, results);
        }
    }

    private void validateScoreMode(
            List<JudgeMilestoneResultMilestoneRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        for (JudgeMilestoneResultMilestoneRequest request : requests) {
            JudgeMilestoneResultEntity result = results.get(request.getId());
            MilestoneCriteriaAssignmentEntity criteria = result.getMilestoneCriteria();

            Preconditions.checkArgument(request.getScore() != null,
                    "Оценка не может быть null для режима SCORE");
            Preconditions.checkArgument(request.getScore() > 0,
                    "Оценка не может быть отрицательной для режима SCORE");
            Preconditions.checkArgument(request.getScore() <= criteria.getScale(),
                    "Оценка %s не может превышать максимальную шкалу %s для критерия %s",
                    request.getScore(), criteria.getScale(), criteria.getCriteria().getName());
        }
    }

    private void validatePassMode(
            MilestoneEntity milestone,
            List<JudgeMilestoneResultMilestoneRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        for (JudgeMilestoneResultMilestoneRequest request : requests) {
            Preconditions.checkArgument(request.getScore() != null,
                    "Оценка не может быть null для режима PASS");
            Preconditions.checkArgument(request.getScore() == 0 || request.getScore() == 1,
                    "Для режима PASS оценка может быть только 0 или 1, получена: %s", request.getScore());
        }

        if (!milestone.getMilestoneOrder().equals(0)) {
            // Получаем количество участников следующего этапа
            int nextMilestoneParticipantLimit = milestoneRepository.getParticipantLimitForNextMilestone(milestone.getActivity().getId(), milestone.getMilestoneOrder());

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
            List<JudgeMilestoneResultMilestoneRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        for (PartnerSide partnerSide : PartnerSide.values()) {
            long countInCurrentMilestone = milestone.getRounds().stream()
                    .flatMap(round -> round.getParticipants().stream())
                    .filter(p -> p.getPartnerSide() == partnerSide).count();

            // Проверяем, что все оценки в диапазоне от 1 до количества участников
            for (JudgeMilestoneResultMilestoneRequest request : requests) {
                Preconditions.checkArgument(request.getScore() != null,
                        "Оценка не может быть null для режима PLACE");
                Preconditions.checkArgument(request.getScore() >= 1 && request.getScore() <= countInCurrentMilestone,
                        "Для режима PLACE оценка должна быть от 1 до %d, получена: %s",
                        countInCurrentMilestone, request.getScore());
            }

            // Проверяем уникальность мест (каждое место может быть только один раз)
            List<Integer> scores = requests.stream()
                    .map(JudgeMilestoneResultMilestoneRequest::getScore)
                    .sorted()
                    .toList();

            long uniqueScoresCount = scores.stream().distinct().count();
            Preconditions.checkArgument(uniqueScoresCount == scores.size(),
                    "В режиме PLACE каждое место должно быть уникальным. Найдены дублирующиеся места: %s", scores);
        }
    }

    @Override
    @Transactional
    public JudgeMilestoneResultDto create(JudgeMilestoneResultRoundRequest request) {
        return createEntity(request);
    }

    @Override
    @Transactional
    public JudgeMilestoneResultDto update(Long id, JudgeMilestoneResultRoundRequest request) {
        return updateEntity(id, request);
    }

    //TODO тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
    private JudgeMilestoneResultDto createEntity(JudgeMilestoneResultRoundRequest request) {
        ParticipantEntity participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(EntityNotFoundException::new);
        MilestoneCriteriaAssignmentEntity milestoneCriteria = milestoneCriteriaAssignmentRepository
                .findById(request.getMilestoneCriteriaId())
                .orElseThrow(EntityNotFoundException::new);
        RoundEntity roundEntity = roundRepository.findById(request.getRoundId())
                .orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = userActivityAssignmentRepository.findByUserIdAndActivityId(
                        currentUser.getSecurityUser().getId(),
                        roundEntity.getMilestone().getActivity().getId())
                .orElseThrow(EntityNotFoundException::new);

        Preconditions.checkArgument(activityUser.getPosition().isJudge(), "Оценивающий должен быть судьей");

        Preconditions.checkArgument(milestoneCriteria.getPartnerSide() == null
                        || activityUser.getPartnerSide() == null
                        || Objects.equals(milestoneCriteria.getPartnerSide(), activityUser.getPartnerSide()),
                "Сторона судьи и критерия не совпадает");

        Preconditions.checkArgument(roundEntity.getParticipants().stream()
                        .anyMatch(p -> p.getId().equals(request.getParticipantId())),
                "Участник не участвует в данном раунде");

        boolean exists = judgeMilestoneResultRepository.existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
                request.getRoundId(), request.getParticipantId(), activityUser.getId(), request.getMilestoneCriteriaId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        JudgeMilestoneResultEntity entity = judgeMilestoneResultRoundRequestMapper.toEntity(request);
        entity.setParticipant(participant);
        entity.setRound(roundEntity);
        entity.setActivityUser(activityUser);
        entity.setMilestoneCriteria(milestoneCriteria);
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);
        return judgeMilestoneResultDtoMapper.toDto(saved);
    }

    private JudgeMilestoneResultDto updateEntity(Long id, JudgeMilestoneResultRoundRequest request) {
        JudgeMilestoneResultEntity entity = judgeMilestoneResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден с id: " + id));

        Preconditions.checkArgument(currentUser.getSecurityUser()
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role == Role.ADMIN || role == Role.SUPERADMIN || role == Role.OCCASION_ADMIN)
                        || entity.getActivityUser().getUser().getId().equals(currentUser.getSecurityUser().getId()),
                "Нельзя изменить результат другого судьи");

        judgeMilestoneResultRoundRequestMapper.updateRoundFromRequest(request, entity);
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);
        return judgeMilestoneResultDtoMapper.toDto(saved);
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

}
