package org.bn.sensation.core.round.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judge.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judge.entity.JudgeRoundStatus;
import org.bn.sensation.core.judge.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.RoundWithJRStatusDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.round.service.mapper.CreateRoundRequestMapper;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.bn.sensation.core.round.service.mapper.RoundWithJRStatusMapper;
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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoundServiceImpl implements RoundService {

    private final CurrentUser currentUser;
    private final CreateRoundRequestMapper createRoundRequestMapper;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final RoundDtoMapper roundDtoMapper;
    private final RoundRepository roundRepository;
    private final RoundWithJRStatusMapper roundWithJRStatusMapper;
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
        log.info("Создание раунда: название={}, этап={}", request.getName(), request.getMilestoneId());
        
        // Проверяем существование этапа
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(request.getMilestoneId())
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));

        log.debug("Найден этап={} для создания раунда", milestone.getId());

        // Создаем сущность раунда
        RoundEntity round = createRoundRequestMapper.toEntity(request);
        round.setMilestone(milestone);

        addParticipants(request.getParticipantIds(), milestone, round);

        RoundEntity saved = roundRepository.save(round);
        log.info("Сохранен раунд с id={}", saved.getId());
        
        // Создаем статусы для судей
        int judgeStatusCount = 0;
        for (UserActivityAssignmentEntity au : milestone.getActivity().getUserAssignments()) {
            if (au.getPosition().isJudge()) {
                JudgeRoundStatusEntity judgeRoundStatus = new JudgeRoundStatusEntity();
                judgeRoundStatus.setRound(saved);
                judgeRoundStatus.setJudge(au);
                judgeRoundStatus.setStatus(JudgeRoundStatus.NOT_READY);
                judgeRoundStatusRepository.save(judgeRoundStatus);
                judgeStatusCount++;
                log.debug("Создан статус судьи для судьи={}, раунда={}", au.getId(), saved.getId());
            }
        }
        
        log.info("Создано {} статусов судей для раунда={}", judgeStatusCount, saved.getId());
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
    public List<RoundWithJRStatusDto> findByMilestoneIdInLifeStates(Long milestoneId) {
        log.info("Поиск раундов в жизненных состояниях для этапа={}, пользователь={}", 
                milestoneId, currentUser.getSecurityUser().getId());
        
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId)
                .orElseThrow(EntityNotFoundException::new);
        
        UserActivityAssignmentEntity judge = milestone.getActivity().getUserAssignments()
                .stream()
                .filter(ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Юзер с id %s не привязан к этапу с id: %s".formatted(currentUser.getSecurityUser().getId(), milestoneId)));
        
        log.debug("Найден судья={} для этапа={}", judge.getId(), milestoneId);
        
        Map<Long, JudgeRoundStatusEntity> judgeRoundEntityMap = judgeRoundStatusRepository.findByMilestoneIdAndJudgeId(milestoneId, judge.getId())
                .stream()
                .collect(Collectors.toMap(jre -> jre.getRound().getId(), Function.identity()));
        
        log.debug("Найдено {} статусов судьи для этапа={}", judgeRoundEntityMap.size(), milestoneId);
        
        List<RoundWithJRStatusDto> result = milestone.getRounds()
                .stream()
                .filter(round -> {
                    boolean isLifeState = RoundState.LIFE_ROUND_STATES.contains(round.getState());
                    log.debug("Раунд={} со статусом={} в жизненных состояниях: {}", 
                            round.getId(), round.getState(), isLifeState);
                    return isLifeState;
                })
                .map(round -> roundWithJRStatusMapper.toDto(round, judgeRoundEntityMap.get(round.getId())))
                .sorted(Comparator.comparing(RoundWithJRStatusDto::getId))
                .toList();
        
        log.info("Найдено {} раундов в жизненных состояниях для этапа={}", result.size(), milestoneId);
        return result;
    }

    @Override
    @Transactional
    public void saveTransition(RoundEntity round, RoundState state) {
        round.setState(state);
        roundRepository.save(round);
    }

    @Override
    public boolean canTransition(RoundEntity round, RoundEvent event) {
        log.debug("Проверка возможности перехода раунда={} из состояния={} по событию={}", 
                round.getId(), round.getState(), event);
        
        return switch (event) {
            case DRAFT, PLAN, COMPLETE -> {
                log.debug("Переход разрешен для события={}", event);
                yield true;
            }
            case START -> {
                log.debug("Проверка возможности старта раунда={}, состояние этапа={}", 
                        round.getId(), round.getMilestone().getState());
                Preconditions.checkState(round.getMilestone().getState() == MilestoneState.IN_PROGRESS,
                        "Нельзя стартовать раунд, т.к. этап находится в статусе %s", round.getMilestone().getState());
                log.debug("Старт раунда разрешен");
                yield true;
            }
            case CONFIRM -> {
                log.debug("Проверка возможности подтверждения раунда={}", round.getId());
                
                if (round.getMilestone().getState() != MilestoneState.IN_PROGRESS) {
                    log.debug("Подтверждение невозможно: этап не в состоянии IN_PROGRESS, текущее состояние={}", 
                            round.getMilestone().getState());
                    yield false;
                }
                
                List<JudgeRoundStatusEntity> judgeRoundStatuses = judgeRoundStatusRepository.findByRoundId(round.getId());
                Set<Long> acceptedJudgeIds = judgeRoundStatuses.stream()
                        .filter(jrs -> jrs.getStatus() == JudgeRoundStatus.READY)
                        .map(jrs -> jrs.getJudge().getUser().getId())
                        .collect(Collectors.toSet());

                Set<Long> requiredJudgeIds = round.getMilestone().getActivity().getUserAssignments()
                        .stream()
                        .filter(ua -> ua.getPosition().isJudge())
                        .map(ua -> ua.getUser().getId())
                        .collect(Collectors.toSet());

                log.debug("Готовых судей={}, требуемых судей={}", acceptedJudgeIds.size(), requiredJudgeIds.size());
                log.debug("ID готовых судей: {}", acceptedJudgeIds);
                log.debug("ID требуемых судей: {}", requiredJudgeIds);

                boolean allJudgesReady = acceptedJudgeIds.containsAll(requiredJudgeIds);
                log.debug("Все судьи готовы: {}", allJudgesReady);
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
