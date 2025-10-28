package org.bn.sensation.core.round.service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.*;
import org.bn.sensation.core.round.service.mapper.CreateRoundRequestMapper;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.bn.sensation.core.round.service.mapper.RoundWithJRStatusMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundRequestMapper;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.core.user.entity.Role;
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
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(request.getMilestoneId());

        log.debug("Найден этап={} для создания раунда", milestone.getId());

        // Создаем сущность раунда
        RoundEntity round = createRoundRequestMapper.toEntity(request);
        round.setMilestone(milestone);

        round.setRoundOrder(roundRepository.getLastRoundOrder(milestone.getId()).orElse(-1) + 1);
        addParticipants(request.getParticipantIds(), milestone, round);

        RoundEntity saved = roundRepository.save(round);
        log.info("Сохранен раунд с id={}", saved.getId());

        // Создаем статусы для судей
        int judgeStatusCount = 0;
        for (ActivityUserEntity au : milestone.getActivity().getUserAssignments()) {
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
        RoundEntity round = roundRepository.getByIdOrThrow(id);
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
        Preconditions.checkArgument(id != null, "ID раунда не может быть null");
        RoundEntity round = roundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + id));
        Integer roundOrder = round.getRoundOrder();
        Set<RoundEntity> reordered = roundRepository.findByMilestoneIdAndRoundOrder(round.getMilestone().getId(), roundOrder)
                .stream().sorted(Comparator.comparing(RoundEntity::getRoundOrder))
                .peek(r -> r.setRoundOrder(r.getRoundOrder() - 1))
                .collect(Collectors.toSet());
        roundRepository.deleteById(id);
        roundRepository.saveAll(reordered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundDto> findByMilestoneId(Long id) {
        Preconditions.checkArgument(id != null, "ID этапа не может быть null");
        return roundRepository.findByMilestoneId(id).stream()
                .sorted(Comparator.comparing(RoundEntity::getRoundOrder))
                .map(roundDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundWithJRStatusDto> findByMilestoneIdInLifeStates(Long milestoneId) {
        log.info("Поиск раундов в жизненных состояниях для этапа={}, пользователь={}",
                milestoneId, currentUser.getSecurityUser().getId());

        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);

        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity judge = ActivityUserUtil.getFromActivity(
                milestone.getActivity(), userId, ua ->
                        ua.getUser()
                                .getId()
                                .equals(currentUser.getSecurityUser().getId())
                                && ua.getPosition().isJudge());

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
                .sorted(Comparator.comparing(RoundWithJRStatusDto::getRoundOrder))
                .toList();

        log.info("Найдено {} раундов в жизненных состояниях для этапа={}", result.size(), milestoneId);
        return result;
    }

    @Override
    @Transactional
    public List<RoundDto> generateRounds(GenerateRoundsRequest request) {
        log.info("Начало генерации раундов для этапа ID={}, перегенерация={}", 
                request.getMilestoneId(), request.getReGenerate());
        
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(request.getMilestoneId());
        log.debug("Этап найден: ID={}, состояние={}, активность ID={}", 
                milestone.getId(), milestone.getState(), milestone.getActivity().getId());
        
        Preconditions.checkState(!Set.of(MilestoneState.IN_PROGRESS, MilestoneState.COMPLETED, MilestoneState.SUMMARIZING).contains(milestone.getState()),
                "Этап находится в состоянии %s и не может быть переформирован", milestone.getState());
        
        if (!Boolean.TRUE.equals(request.getReGenerate())) {
            Preconditions.checkArgument(milestone.getParticipants().isEmpty() && milestone.getRounds().isEmpty(),
                    "Этап уже содержит участников или раунды");
        } else {
            milestone.getParticipants().clear();
            roundRepository.deleteAll(milestone.getRounds());
            milestone.getRounds().clear();
        }
        
        List<ParticipantEntity> participants;
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            log.info("Генерация раундов для конкретных участников: {}", request.getParticipantIds());
            participants = participantRepository.findByActivityIdAndIdIn(milestone.getActivity().getId(), request.getParticipantIds())
                    .stream()
                    .peek(participant -> {
                        Preconditions.checkArgument(participant.getIsRegistered(), "Участник с ID %s не зарегистрирован".formatted(participant.getId()));
                    })
                    .sorted(Comparator.comparing(ParticipantEntity::getNumber).reversed())
                    .toList();

            if (participants.size() != request.getParticipantIds().size()) {
                participants.stream().map(ParticipantEntity::getId).forEach(request.getParticipantIds()::remove);
                throw new IllegalArgumentException("Не все участники из запроса найдены для активности ID %s. Не найдены: %s"
                        .formatted(milestone.getActivity().getId(), request.getParticipantIds()));
            }
        } else {
            log.info("Генерация раундов для всех зарегистрированных участников активности");
            participants = participantRepository.findByActivityId(milestone.getActivity().getId())
                    .stream()
                    .filter(participant -> participant.getIsRegistered())
                    .sorted(Comparator.comparing(ParticipantEntity::getNumber).reversed())
                    .toList();
        }
        
        log.info("Найдено участников для генерации: {}", participants.size());
        if (participants.isEmpty()) {
            log.warn("Нет зарегистрированных участников для генерации раундов");
            return Collections.emptyList();
        }
        
        milestone.getParticipants().addAll(participants);
        List<RoundEntity> rounds = roundRepository.saveAll(generate(participants, milestone));
        milestone.getRounds().addAll(rounds);
        milestoneRepository.save(milestone);
        
        log.info("Успешно сгенерировано {} раундов для этапа ID={}", rounds.size(), milestone.getId());
        return rounds.stream().map(roundDtoMapper::toDto).toList();
    }

    private List<RoundEntity> generate(List<ParticipantEntity> participants, MilestoneEntity milestone) {
        log.debug("Начало генерации раундов для {} участников", participants.size());
        
        List<ParticipantEntity> leaders = new ArrayList<>(participants.stream()
                .filter(p -> p.getPartnerSide() == PartnerSide.LEADER)
                .toList());
        List<ParticipantEntity> followers = new ArrayList<>(participants.stream()
                .filter(p -> p.getPartnerSide() == PartnerSide.FOLLOWER)
                .toList());
        
        log.debug("Разделение участников: лидеров={}, последователей={}", leaders.size(), followers.size());
        
        if (leaders.isEmpty() && followers.isEmpty()) {
            log.warn("Нет участников для генерации раундов");
            return Collections.emptyList();
        }
        
        boolean distribute = false;
        int roundLimit = milestone.getMilestoneRule().getRoundParticipantLimit().intValue();
        log.debug("Лимит участников на раунд: {}", roundLimit);
        int dividend = Math.max(leaders.size(), followers.size());
        int roundCount = dividend / roundLimit;
        int remainder = dividend % roundLimit;
        log.debug("Расчет раундов: всего участников={}, раундов={}, остаток={}", 
                dividend, roundCount, remainder);
        if (remainder <= roundLimit / 2 && remainder <= roundCount) {
            distribute = true;
            log.debug("Остаток будет распределен по существующим раундам");
        } else {
            roundCount++;
            log.debug("Создается дополнительный раунд для остатка");
        }
        log.info("Будет создано {} раундов", roundCount);
        
        List<RoundEntity> roundEntities = new ArrayList<>();
        for (int i = 0; i < roundCount; i++) {
            RoundEntity round = RoundEntity.builder()
                    .roundOrder(i)
                    .extraRound(false)
                    .milestone(milestone)
                    .state(RoundState.PLANNED)
                    .name("Раунд %s".formatted(i + 1))
                    .build();
            
            log.debug("Создание раунда {}", round.getName());
            int j = 0;
            while (j < roundLimit && (!leaders.isEmpty() || !followers.isEmpty())) {
                addToRound(leaders, followers, round);
                j++;
                log.trace("Добавлен участник в раунд {}, всего участников: {}", i + 1, round.getParticipants().size());
            }
            if (distribute) {
                addToRound(leaders, followers, round);
                log.debug("Добавлен дополнительный участник в раунд {} (распределение остатка)", i + 1);
            }
            
            log.debug("Раунд {} сформирован: {} участников", i, round.getParticipants().size());
            roundEntities.add(round);
        }
        
        log.info("Генерация завершена: создано {} раундов", roundEntities.size());
        return roundEntities;
    }

    private void addToRound(List<ParticipantEntity> leaders, List<ParticipantEntity> followers, RoundEntity round) {
        if (!leaders.isEmpty()) {
            ParticipantEntity leader = leaders.get(leaders.size()-1);
            round.getParticipants().add(leader);
            leaders.remove(leaders.size()-1);
            log.trace("Добавлен лидер ID={} в раунд {}", leader.getId(), round.getName());
        }
        if (!followers.isEmpty()) {
            ParticipantEntity follower = followers.get(followers.size()-1);
            round.getParticipants().add(follower);
            followers.remove(followers.size()-1);
            log.trace("Добавлен последователь ID={} в раунд {}", follower.getId(), round.getName());
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
            case MARK_READY -> {
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
                case MARK_READY -> RoundState.READY;
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

    /**
     * Не должно применяться в нормальном флоу. Нужно на экстренный случай
     */
    private void addParticipants(List<Long> participantIds, MilestoneEntity milestone, RoundEntity round) {
        log.warn("Добавление участников в раунд вне нормального флоу: milestone={}, round={}, participantIds={}", milestone.getId(), round.getId(), participantIds);
        Preconditions.checkArgument(currentUser.getSecurityUser().getRoles().contains(Role.SUPERADMIN), "Только суперадмин может привязывать участников напрямую");
        if (participantIds != null && !participantIds.isEmpty()) {
            Set<ParticipantEntity> participants = participantRepository.findAllByIdWithActivity(participantIds)
                    .stream()
                    .peek(participant -> {
                        Preconditions.checkArgument(participant.getIsRegistered(), "Может быть добавлен только зарегистрированный участник");
                        Preconditions.checkArgument(participant.getActivity().getId().equals(milestone.getActivity().getId()),
                                "Участник с ID %s не принадлежит активности %s", participant.getId(), milestone.getActivity().getId());
                        Preconditions.checkArgument(participant.getMilestones().stream().anyMatch(m -> m.getId().equals(milestone.getId())),
                                "Участник с ID %s должен быть сначала добавлен в этап раунда с ID: %s", participant.getId(), milestone.getId());
                    })
                    .collect(Collectors.toSet());
            round.getParticipants().addAll(participants);
        }
    }
}
