package org.bn.sensation.core.judgemilstoneresult.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.judgeroundstatus.service.JudgeRoundStatusService;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusService;
import org.bn.sensation.core.milestonecriteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.judgemilstoneresult.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.judgemilestonestatus.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgemilstoneresult.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.bn.sensation.core.judgemilstoneresult.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.judgemilstoneresult.service.mapper.JudgeMilestoneResultDtoMapper;
import org.bn.sensation.core.judgemilstoneresult.service.mapper.JudgeMilestoneResultMilestoneRequestMapper;
import org.bn.sensation.core.judgemilstoneresult.service.mapper.JudgeMilestoneResultRoundRequestMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.RoundStateMachineService;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.useractivity.entity.UserActivityAssignmentEntity;
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
    private final JudgeMilestoneResultMilestoneRequestMapper judgeMilestoneResultMilestoneRequestMapper;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final JudgeMilestoneResultRoundRequestMapper judgeMilestoneResultRoundRequestMapper;
    //TODO может быть сделать через ApplicationEvents потом
    private final JudgeMilestoneStatusService judgeMilestoneStatusService;
    //TODO может быть сделать через ApplicationEvents потом
    private final JudgeRoundStatusService judgeRoundStatusService;
    private final MilestoneRepository milestoneRepository;
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
    public JudgeMilestoneResultDto createOrUpdate(JudgeMilestoneResultRoundRequest request, Long activityUserId) {
        log.info("Создание или обновление результата судьи для раунда={}, участника={}, судьи={}",
                request.getRoundId(), request.getParticipantId(), activityUserId);

        if (request.getId() == null) {
            log.debug("Создание нового результата судьи");
            Preconditions.checkArgument(activityUserId != null, "ID судьи не может быть null");
            RoundEntity round = roundRepository.findByIdFullEntity(request.getRoundId())
                    .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + request.getRoundId()));
            UserActivityAssignmentEntity activityUser = round.getMilestone().getActivity().getUserAssignments().stream()
                    .filter(ua -> ua.getId().equals(activityUserId))
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);
            log.debug("Найден раунд={} и судья={} для создания", round.getId(), activityUser.getId());
            return createEntity(request, round, activityUser);
        } else {
            log.debug("Обновление существующего результата судьи с id={}", request.getId());
            return updateEntity(request, activityUserId);
        }
    }

    @Override
    @Transactional
    public List<JudgeMilestoneResultDto> createOrUpdateForRound(Long roundId, List<JudgeMilestoneResultRoundRequest> requests) {
        log.info("Создание или обновление результатов судьи для раунда={}, количество запросов={}",
                roundId, requests != null ? requests.size() : 0);

        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        RoundEntity round = roundRepository.findByIdFullEntity(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + roundId));

        UserActivityAssignmentEntity activityUser = round.getMilestone().getActivity().getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Судья не найден с user id: " + currentUser.getSecurityUser().getId()));

        log.debug("Найден судья={} для раунда={}, сторона={}",
                activityUser.getId(), roundId, activityUser.getPartnerSide());

        validateResultsCount(roundId, requests, round, activityUser);

        List<JudgeMilestoneResultDto> dtos = new ArrayList<>();
        if (requests != null && !requests.isEmpty()) {
            log.debug("Обработка {} запросов для раунда {}", requests.size(), roundId);
            requests.forEach(request -> {
                if (request.getId() == null) {
                    log.debug("Создание нового результата для участника={}, критерия={}",
                            request.getParticipantId(), request.getMilestoneCriteriaId());
                    dtos.add(createEntity(request, round, activityUser));
                } else {
                    log.debug("Обновление существующего результата с id={}", request.getId());
                    dtos.add(updateEntity(request, activityUser.getId()));
                }
            });

            log.info("Изменение статуса раунда судьи на READY для судьи={}, раунда={}",
                    activityUser.getId(), roundId);
            judgeRoundStatusService.changeJudgeRoundStatusIfPossible(activityUser.getId(), roundId, JudgeRoundStatus.READY);
            roundStateMachineService.sendEvent(roundId, RoundEvent.MARK_READY);
        }

        if (judgeMilestoneStatusService.allRoundsReady(round.getMilestone().getId())) {
            log.info("Все раунды готовы для этапа={}, изменение статуса на READY", round.getMilestone().getId());
            judgeMilestoneStatusService.changeMilestoneStatus(round.getMilestone(), activityUser, JudgeMilestoneStatus.READY);
        }

        log.info("Успешно обработано {} результатов для раунда {}", dtos.size(), roundId);
        return dtos;
    }

    private void validateResultsCount(Long roundId, List<JudgeMilestoneResultRoundRequest> requests, RoundEntity round, UserActivityAssignmentEntity activityUser) {
        log.debug("Проверка количества результатов для раунда={}, судья={}, сторона судьи={}", roundId, activityUser.getId(), activityUser.getPartnerSide());

        List<JudgeMilestoneResultEntity> resultsByRound = judgeMilestoneResultRepository.findByRoundIdAndActivityUserId(roundId, activityUser.getId());
        long toCreate = requests.stream().filter(request -> request.getId() == null).count();
        long participantsCount = round.getParticipants().stream().filter(p -> {
            if (activityUser.getPartnerSide() != null) {
                return p.getPartnerSide() == activityUser.getPartnerSide();
            }
            return true;
        }).count();

        long criteriaCount = round.getMilestone().getMilestoneRule().getCriteriaAssignments().stream()
                .filter(ca -> ca.getPartnerSide() == null || ca.getPartnerSide() == activityUser.getPartnerSide())
                .count();

        long resultsTotalCount = participantsCount * criteriaCount;

        log.debug("Результаты проверки: существующих={}, к созданию={}, участников={}, критериев={}, всего требуется={}",
                resultsByRound.size(), toCreate, participantsCount, criteriaCount, resultsTotalCount);

        Preconditions.checkArgument(resultsByRound.size() + toCreate == resultsTotalCount, "Все участники раунда должны быть оценены");
    }

    //TODO тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
    private JudgeMilestoneResultDto createEntity(JudgeMilestoneResultRoundRequest request, RoundEntity roundEntity, UserActivityAssignmentEntity activityUser) {
        log.debug("Создание сущности результата судьи для раунда={}, участника={}, критерия={}, оценки={}",
                request.getRoundId(), request.getParticipantId(), request.getMilestoneCriteriaId(), request.getScore());

        Preconditions.checkArgument(roundEntity.getId().equals(request.getRoundId()),
                "ID раунда не может быть null или отличаться от целевого раунда");
        Preconditions.checkArgument(request.getParticipantId() != null, "ID участника не может быть null");
        Preconditions.checkArgument(request.getMilestoneCriteriaId() != null, "ID критерия не может быть null");

        ParticipantEntity participant = roundEntity.getParticipants().stream()
                .filter(p -> p.getId().equals(request.getParticipantId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Участник не участвует в данном раунде"));
        MilestoneCriteriaAssignmentEntity milestoneCriteria = roundEntity.getMilestone().getMilestoneRule().getCriteriaAssignments().stream()
                .filter(ca -> ca.getId().equals(request.getMilestoneCriteriaId()))
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);

        log.debug("Найден участник={} и критерий={} для создания результата", participant.getId(), milestoneCriteria.getId());

        Preconditions.checkArgument(activityUser.getPosition().isJudge(), "Оценивающий должен быть судьей");

        //TODO тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
        Preconditions.checkArgument(milestoneCriteria.getPartnerSide() == null
                        || activityUser.getPartnerSide() == null
                        || Objects.equals(milestoneCriteria.getPartnerSide(), activityUser.getPartnerSide()),
                "Сторона судьи и критерия не совпадает");
        boolean exists = judgeMilestoneResultRepository.existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
                request.getRoundId(), request.getParticipantId(), activityUser.getId(), request.getMilestoneCriteriaId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        JudgeMilestoneResultEntity entity = judgeMilestoneResultRoundRequestMapper.toEntity(request);
        entity.setParticipant(participant);
        entity.setRound(roundEntity);
        entity.setActivityUser(activityUser);
        entity.setMilestoneCriteria(milestoneCriteria);
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);

        log.info("Успешно создан результат судьи с id={} для участника={}, критерия={}, оценки={}",
                saved.getId(), participant.getId(), milestoneCriteria.getId(), request.getScore());

        return judgeMilestoneResultDtoMapper.toDto(saved);
    }

    private JudgeMilestoneResultDto updateEntity(JudgeMilestoneResultRoundRequest request, Long activityUserId) {
        JudgeMilestoneResultEntity entity = judgeMilestoneResultRepository.findByIdWithUser(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден с id: " + request.getId()));

        Preconditions.checkArgument(currentUser.getSecurityUser()
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role == Role.ADMIN || role == Role.SUPERADMIN || role == Role.OCCASION_ADMIN)
                        || entity.getActivityUser().getId().equals(activityUserId),
                "Нельзя изменить результат другого судьи");

        judgeMilestoneResultRoundRequestMapper.updateJudgeMilestoneResultRoundFromRequest(request, entity);
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);
        return judgeMilestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<JudgeMilestoneResultDto> createOrUpdateForMilestone(Long milestoneId, List<JudgeMilestoneResultMilestoneRequest> requests) {
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId)
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
            judgeMilestoneResultMilestoneRequestMapper.updateJudgeMilestoneResultMilestoneFromRequest(request, results.get(request.getId()));
            updated.add(judgeMilestoneResultRepository.save(results.get(request.getId())));
        });
        updated.stream().map(res -> res.getRound().getId()).distinct().forEach(roundId -> {
            judgeRoundStatusService.changeJudgeRoundStatusIfPossible(activityUser.getId(), roundId, JudgeRoundStatus.READY);
        });
        judgeMilestoneStatusService.changeMilestoneStatus(milestone, activityUser, JudgeMilestoneStatus.READY);
        return updated.stream().map(judgeMilestoneResultDtoMapper::toDto).toList();
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
        RoundEntity round = roundRepository.findByIdWithUserAssignments(roundId)
                .orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = round.getMilestone().getActivity().getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Данный юзер не зарегистрирован для раунда " + roundId));
        return judgeMilestoneResultRepository.findByRoundIdAndActivityUserId(roundId, activityUser.getId()).stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    public List<JudgeMilestoneResultDto> findByMilestoneIdForCurrentUser(Long milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId)
                .orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = milestone.getActivity().getUserAssignments().stream()
                .filter(ua -> ua.getUser().getId().equals(currentUser.getSecurityUser().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Данный юзер не зарегистрирован для этапа " + milestoneId));
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
        log.debug("Проверка режима SCORE для {} запросов", requests.size());

        for (JudgeMilestoneResultMilestoneRequest request : requests) {
            JudgeMilestoneResultEntity result = results.get(request.getId());
            MilestoneCriteriaAssignmentEntity criteria = result.getMilestoneCriteria();

            log.debug("Проверка оценки={} для критерия={} со шкалой={}",
                    request.getScore(), criteria.getCriteria().getName(), criteria.getScale());

            Preconditions.checkArgument(request.getScore() != null,
                    "Оценка не может быть null для режима SCORE");
            Preconditions.checkArgument(request.getScore() > 0,
                    "Оценка не может быть отрицательной для режима SCORE");
            Preconditions.checkArgument(request.getScore() <= criteria.getScale(),
                    "Оценка %s не может превышать максимальную шкалу %s для критерия %s",
                    request.getScore(), criteria.getScale(), criteria.getCriteria().getName());
        }

        log.debug("Проверка режима SCORE завершена успешно");
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
            List<JudgeMilestoneResultMilestoneRequest> requests,
            Map<Long, JudgeMilestoneResultEntity> results
    ) {
        for (PartnerSide partnerSide : PartnerSide.values()) {
            long countInCurrentMilestone = milestone.getRounds().stream()
                    .flatMap(round -> round.getParticipants().stream())
                    .filter(p -> p.getPartnerSide() == partnerSide).count();
            List<JudgeMilestoneResultMilestoneRequest> partnerSideRequests = requests.stream()
                    .filter(req -> results.get(req.getId()).getParticipant().getPartnerSide() == partnerSide)
                    .toList();
            // Проверяем, что все оценки в диапазоне от 1 до количества участников
            for (JudgeMilestoneResultMilestoneRequest request : partnerSideRequests) {
                Preconditions.checkArgument(request.getScore() != null,
                        "Оценка не может быть null для режима PLACE");
                Preconditions.checkArgument(request.getScore() >= 1 && request.getScore() <= countInCurrentMilestone,
                        "Для режима PLACE оценка должна быть от 1 до %s, получена: %s",
                        countInCurrentMilestone, request.getScore());
            }

            // Проверяем уникальность мест (каждое место может быть только один раз)
            List<Integer> scores = partnerSideRequests.stream()
                    .map(JudgeMilestoneResultMilestoneRequest::getScore)
                    .sorted()
                    .toList();

            long uniqueScoresCount = scores.stream().distinct().count();
            Preconditions.checkArgument(uniqueScoresCount == scores.size(),
                    "В режиме PLACE каждое место должно быть уникальным. Найдены дублирующиеся места: %s", scores);
        }
    }

    @Override
    public JudgeMilestoneResultDto create(JudgeMilestoneResultRoundRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JudgeMilestoneResultDto update(Long id, JudgeMilestoneResultRoundRequest request) {
        throw new UnsupportedOperationException();
    }
}
