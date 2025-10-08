package org.bn.sensation.core.round.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.milestone.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.round.entity.JudgeRoundEntity;
import org.bn.sensation.core.round.entity.JudgeRoundStatus;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.JudgeRoundRepository;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.JudgeRoundDto;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.round.service.mapper.CreateRoundRequestMapper;
import org.bn.sensation.core.round.service.mapper.JudgeRoundMapper;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundRequestMapper;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
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
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final RoundDtoMapper roundDtoMapper;
    private final CreateRoundRequestMapper createRoundRequestMapper;
    private final UpdateRoundRequestMapper updateRoundRequestMapper;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final JudgeRoundRepository judgeRoundRepository;
    private final JudgeRoundMapper judgeRoundMapper;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<RoundEntity> getRepository() {
        return roundRepository;
    }

    @Override
    public BaseDtoMapper<RoundEntity, RoundDto> getMapper() {
        return roundDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoundDto> findAll(Pageable pageable) {
        return roundRepository.findAll(pageable).map(roundDtoMapper::toDto);
    }

    @Override
    @Transactional
    public RoundDto create(CreateRoundRequest request) {
        // Проверяем существование этапа
        MilestoneEntity milestone = milestoneRepository.findByIdWithActivity(request.getMilestoneId())
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));

        // Создаем сущность раунда
        RoundEntity round = createRoundRequestMapper.toEntity(request);
        round.setMilestone(milestone);

        addParticipants(request.getParticipantIds(), milestone, round);

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoundDto update(Long id, UpdateRoundRequest request) {
        RoundEntity round = roundRepository.findByIdWithActivity(id)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + id));

        if (request.getName() != null) {
            Preconditions.checkArgument(!request.getName().trim().isEmpty(), "Название раунда не может быть пустым");
        }

        updateRoundRequestMapper.updateRoundFromRequest(request, round);

        addParticipants(request.getParticipantIds(), round.getMilestone(), round);

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!roundRepository.existsById(id)) {
            throw new EntityNotFoundException("Раунд не найден с id: " + id);
        }
        roundRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundDto> findByMilestoneId(Long id) {
        Preconditions.checkArgument(id != null, "ID этапа не может быть null");
        return roundRepository.findByMilestoneId(id).stream()
                .map(roundDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundDto> findByMilestoneIdInLifeStates(Long id) {
        Preconditions.checkArgument(id != null, "ID этапа не может быть null");
        return roundRepository.findByMilestoneIdAndStateIn(id, RoundState.LIFE_ROUND_STATES).stream()
                .map(roundDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public JudgeRoundDto changeRoundStatus(Long roundId, JudgeRoundStatus judgeRoundStatus) {
        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        Preconditions.checkArgument(judgeRoundStatus != null, "Статус не может быть null");

        RoundEntity round = roundRepository.findByIdWithUserAssignments(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + roundId));
        UserActivityAssignmentEntity activityAssignment = round.getMilestone().getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к раунду с id: %s".formatted(currentUser.getSecurityUser().getId(), roundId)));
        Preconditions.checkState(round.getState() == RoundState.IN_PROGRESS,
                "Статус раунда %s. Не может быть принят или отменен судьей", round.getState());

        checkAccepted(judgeRoundStatus, activityAssignment, round);

        JudgeRoundEntity judgeRoundEntity = judgeRoundRepository.findByRoundIdAndJudgeId(roundId, activityAssignment.getId())
                .orElse(JudgeRoundEntity.builder().round(round).judge(activityAssignment).build());
        judgeRoundEntity.setStatus(judgeRoundStatus);
        return judgeRoundMapper.toDto(judgeRoundRepository.save(judgeRoundEntity));
    }

    private void checkAccepted(JudgeRoundStatus judgeRoundStatus, UserActivityAssignmentEntity activityAssignment, RoundEntity round) {
        if (judgeRoundStatus == JudgeRoundStatus.ACCEPTED) {
            List<JudgeMilestoneResultEntity> results = judgeMilestoneResultRepository.findByActivityUserId(activityAssignment.getId());
            if (activityAssignment.getPartnerSide() == null) {
                Preconditions.checkState(round.getParticipants().size() == results.size(),
                        "Судья оценил не всех участников. Оценено: %s. Всего участников в раунде: %s",
                        results.size(), round.getParticipants().size());
            } else {
                //TODO добавить смену сторон, если есть правило этапа на смену сторон судей
                long participantsCount = round.getParticipants()
                        .stream()
                        .filter(p -> p.getPartnerSide() == activityAssignment.getPartnerSide())
                        .count();
                long resultsCount = results.stream()
                        .filter(prr -> prr.getParticipant().getPartnerSide() == activityAssignment.getPartnerSide())
                        .count();
                Preconditions.checkState(participantsCount == resultsCount,
                        "Судья оценил не всех участников своей стороны. Оценено: %s. Всего участников в раунде: %s",
                        results.size(), round.getParticipants().size());
            }
        }
    }

    @Override
    @Transactional
    public void saveTransition(RoundEntity round, RoundState state) {
        round.setState(state);
        roundRepository.save(round);
    }

    @Override
    public boolean canTransition(RoundEntity round, RoundEvent event) {
        return switch (event) {
            //TODO сделать для COMPLETE
            case PLAN, COMPLETE -> true;
            case START -> {
                Preconditions.checkState(round.getMilestone().getState() == MilestoneState.IN_PROGRESS,
                        "Нельзя стартовать раунд, т.к. этап находится в статусе %s", round.getMilestone().getState());
                yield true;
            }
            case ACCEPT -> {
                List<JudgeRoundEntity> judgeRoundStatuses = judgeRoundRepository.findByRoundId(round.getId());
                Set<Long> acceptedJudgeIds = judgeRoundStatuses.stream()
                        .filter(jrs -> jrs.getStatus() == JudgeRoundStatus.ACCEPTED)
                        .map(jrs -> jrs.getJudge().getUser().getId())
                        .collect(Collectors.toSet());

                Set<Long> requiredJudgeIds = round.getMilestone().getActivity().getUserAssignments()
                        .stream()
                        .filter(ua -> ua.getPosition().isJudge())
                        .map(ua -> ua.getUser().getId())
                        .collect(Collectors.toSet());

                boolean allJudgesAccepted = acceptedJudgeIds.containsAll(requiredJudgeIds);
                Preconditions.checkState(allJudgesAccepted, "Не все судьи подтвердили результаты. Раунд не может быть завершен");
                yield true;
            }
        };
    }

    @Override
    public RoundState getNextState(RoundState currentState, RoundEvent event) {
        return switch (currentState) {
            case DRAFT -> event == RoundEvent.PLAN ? RoundState.PLANNED : currentState;
            case PLANNED -> event == RoundEvent.START ? RoundState.IN_PROGRESS : currentState;
            case IN_PROGRESS, COMPLETED -> event == RoundEvent.ACCEPT ? RoundState.ACCEPTED : currentState;
            case ACCEPTED -> event == RoundEvent.COMPLETE
                    ? RoundState.COMPLETED :
                    event == RoundEvent.PLAN
                            ? RoundState.IN_PROGRESS
                            : currentState;
        };
    }

    @Override
    public boolean isValidTransition(RoundState currentState, RoundEvent event) {
        return getNextState(currentState, event) != currentState;
    }

    private void addParticipants(List<Long> participantIds, MilestoneEntity milestone, RoundEntity round) {
        if (participantIds != null && !participantIds.isEmpty()) {
            Set<ParticipantEntity> participants = participantRepository.findAllByIdWithActivity(participantIds)
                    .stream()
                    .peek(participant -> {
                        Preconditions.checkArgument(participant.getActivity().getId().equals(milestone.getActivity().getId()),
                                "Участник с ID %s не принадлежит активности %s", participant.getId(), milestone.getActivity().getId());
                    })
                    .collect(Collectors.toSet());
            round.setParticipants(participants);
        }
    }
}
