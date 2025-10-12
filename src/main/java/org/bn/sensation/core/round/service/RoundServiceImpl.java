package org.bn.sensation.core.round.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judge.entity.JudgeRoundEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.judge.repository.JudgeRoundRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.round.service.mapper.CreateRoundRequestMapper;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundServiceImpl implements RoundService {

    private final CreateRoundRequestMapper createRoundRequestMapper;
    private final JudgeRoundRepository judgeRoundRepository;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final RoundDtoMapper roundDtoMapper;
    private final RoundRepository roundRepository;
    private final UpdateRoundRequestMapper updateRoundRequestMapper;

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
    public void saveTransition(RoundEntity round, RoundState state) {
        round.setState(state);
        roundRepository.save(round);
    }

    @Override
    public boolean canTransition(RoundEntity round, RoundEvent event) {
        return switch (event) {
            case DRAFT, PLAN, COMPLETE -> true;
            case START -> {
                Preconditions.checkState(round.getMilestone().getState() == MilestoneState.IN_PROGRESS,
                        "Нельзя стартовать раунд, т.к. этап находится в статусе %s", round.getMilestone().getState());
                yield true;
            }
            case CONFIRM -> {
                if (round.getMilestone().getState() != MilestoneState.IN_PROGRESS) {
                    yield false;
                }
                List<JudgeRoundEntity> judgeRoundStatuses = judgeRoundRepository.findByRoundId(round.getId());
                Set<Long> acceptedJudgeIds = judgeRoundStatuses.stream()
                        .filter(jrs -> jrs.getStatus() == JudgeRoundStatus.READY)
                        .map(jrs -> jrs.getJudge().getUser().getId())
                        .collect(Collectors.toSet());

                Set<Long> requiredJudgeIds = round.getMilestone().getActivity().getUserAssignments()
                        .stream()
                        .filter(ua -> ua.getPosition().isJudge())
                        .map(ua -> ua.getUser().getId())
                        .collect(Collectors.toSet());

                boolean allJudgesReady = acceptedJudgeIds.containsAll(requiredJudgeIds);
                yield allJudgesReady;
            }
        };
    }

    @Override
    public RoundState getNextState(RoundState currentState, RoundEvent event) {
        return switch (currentState) {
            case DRAFT -> event == RoundEvent.PLAN ? RoundState.PLANNED : currentState;
            case PLANNED -> switch (event) {
                case DRAFT -> RoundState.DRAFT;
                case START -> RoundState.IN_PROGRESS;
                default -> currentState;
            };
            case IN_PROGRESS -> switch (event) {
                case PLAN -> RoundState.PLANNED;
                case CONFIRM -> RoundState.READY;
                default -> currentState;
            };
            case READY -> switch (event) {
                case START -> RoundState.IN_PROGRESS;
                case COMPLETE -> RoundState.COMPLETED;
                default -> currentState;
            };
            case COMPLETED -> event == RoundEvent.START ? RoundState.IN_PROGRESS : currentState;
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
