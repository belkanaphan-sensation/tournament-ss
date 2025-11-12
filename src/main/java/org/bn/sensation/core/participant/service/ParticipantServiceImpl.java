package org.bn.sensation.core.participant.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.activity.statemachine.ActivityState;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.round.statemachine.RoundState;
import org.bn.sensation.core.judgemilestonestatus.service.JudgeMilestoneStatusCacheService;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatus;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.judgeroundstatus.repository.JudgeRoundStatusRepository;
import org.bn.sensation.core.judgeroundstatus.service.JudgeRoundStatusService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.RoundParticipantsDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.bn.sensation.core.participant.service.mapper.CreateParticipantRequestMapper;
import org.bn.sensation.core.participant.service.mapper.ParticipantDtoMapper;
import org.bn.sensation.core.participant.service.mapper.RoundParticipantsDtoMapper;
import org.bn.sensation.core.participant.service.mapper.UpdateParticipantRequestMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
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
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;
    private final ActivityRepository activityRepository;
    private final ParticipantDtoMapper participantDtoMapper;
    private final CreateParticipantRequestMapper createParticipantRequestMapper;
    private final UpdateParticipantRequestMapper updateParticipantRequestMapper;
    private final JudgeRoundStatusRepository judgeRoundStatusRepository;
    private final JudgeRoundStatusService judgeRoundStatusService;
    private final JudgeMilestoneStatusCacheService judgeMilestoneStatusCacheService;
    private final RoundParticipantsDtoMapper roundParticipantsDtoMapper;
    private final CurrentUser currentUser;
    private final MilestoneRepository milestoneRepository;

    @Override
    public BaseRepository<ParticipantEntity> getRepository() {
        return participantRepository;
    }

    @Override
    public BaseDtoMapper<ParticipantEntity, ParticipantDto> getMapper() {
        return participantDtoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParticipantDto> findAll(Pageable pageable) {
        return participantRepository.findAll(pageable).map(participantDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDto> findByRoundId(Long roundId) {
        return participantRepository.findByRoundId(roundId).stream()
                .map(p -> participantDtoMapper.toDto(p))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDto> findByActivityId(Long activityId) {
        return participantRepository.findByActivityId(activityId).stream()
                .map(p -> participantDtoMapper.toDto(p))
                .toList();
    }

    @Override
    @Transactional
    public ParticipantDto create(CreateParticipantRequest request) {
        ActivityEntity activity = activityRepository.getByIdOrThrow(request.getActivityId());

        ParticipantEntity participant = createParticipantRequestMapper.toEntity(request);
        participant.setActivity(activity);

        if (Boolean.TRUE.equals(participant.getIsRegistered())) {
            Preconditions.checkArgument(Strings.isNotBlank(participant.getNumber()), "Участник должен иметь стартовый номер");
        }

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(participantRepository.getByIdFullOrThrow(saved.getId()));
    }

    @Override
    @Transactional
    public ParticipantDto update(Long id, UpdateParticipantRequest request) {
        ParticipantEntity participant = participantRepository.getByIdFullOrThrow(id);

        Preconditions.checkState(ActivityState.PLANNED == participant.getActivity().getState(),
                "Данные об участнике %s не могут быть обновлены т.к. он находится в активности с состоянием %s", request.getActivityId(), participant.getActivity().getState());
        updateParticipantRequestMapper.updateParticipantFromRequest(request, participant);

        if (Boolean.TRUE.equals(request.getIsRegistered())) {
            Preconditions.checkArgument(Strings.isNotBlank(request.getNumber()), "Участник должен иметь стартовый номер");
        } else if (Boolean.FALSE.equals(request.getIsRegistered())){
            participant.setNumber(null);
        }
        if (request.getActivityId() != null) {
            ActivityEntity activity = activityRepository.getByIdOrThrow(request.getActivityId());
            Preconditions.checkArgument(activity.getOccasion().getId().equals(participant.getActivity().getOccasion().getId()),
                    "Участник не может быть привязан к активности %s т.к. она находится в другом мероприятии", request.getActivityId());
            Preconditions.checkState(ActivityState.PLANNED == activity.getState(),
                    "Участник не может быть привязан к активности %s т.к. она находится в неподходящем состоянии %s", request.getActivityId(), activity.getState());
            participant.setActivity(activity);
        }

        participantRepository.save(participant);
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new EntityNotFoundException("Участник не найден с id: " + id);
        }
        participantRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ParticipantDto assignParticipantToRound(Long participantId, Long roundId) {
        ParticipantEntity participant = participantRepository.getByIdFullOrThrow(participantId);
        Preconditions.checkArgument(participant.getIsRegistered(), "Участник не закончил регистрацию");
        RoundEntity round = roundRepository.getByIdOrThrow(roundId);
        Preconditions.checkState(round.getState() == RoundState.OPENED,
                "Нельзя привязать участника к раунду т.к. раунд в состоянии %s", round.getState());
        Set<Long> milestoneIds = participant.getMilestones().stream().map(MilestoneEntity::getId).collect(Collectors.toSet());
        if (!milestoneIds.contains(round.getMilestone().getId())) {
            assignParticipantToMilestone(participant, round.getMilestone());
        }
        if (!round.getExtraRound()) {
            Preconditions.checkArgument(!participantRepository.existsByParticipantIdAndMilestoneId(participantId, round.getMilestone().getId()),
                    "Участник %s уже привязан к другому раунду этапа %s", participantId, round.getMilestone().getId());
        }
        participant.getRounds().add(round);
        participantRepository.save(participant);
        List<JudgeRoundStatusEntity> statuses = judgeRoundStatusService.getByRoundId(round.getId());
        statuses.forEach(jr -> jr.setStatus(JudgeRoundStatus.NOT_READY));
        judgeRoundStatusRepository.saveAll(statuses);

        judgeRoundStatusService.invalidateForRound(round.getId());
        log.debug("Инвалидирован кэш статуса раунда roundId={}", round.getId());
        judgeMilestoneStatusCacheService.invalidateForMilestone(round.getMilestone().getId());
        log.debug("Инвалидирован кэш статуса этапа milestoneId={} при переводе раунда в черновик", round.getMilestone().getId());
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional
    public ParticipantDto removeParticipantFromRound(Long participantId, Long roundId) {
        ParticipantEntity participant = participantRepository.getByIdOrThrow(participantId);
        RoundEntity round = roundRepository.getByIdOrThrow(roundId);

        Preconditions.checkState(round.getState() == RoundState.OPENED,
                "Нельзя отвязать участника от раунда т.к. раунд в состоянии %s", round.getState());
        participant.getRounds().remove(round);
        participantRepository.save(participant);
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional
    public ParticipantDto assignParticipantToMilestone(Long participantId, Long milestoneId) {
        ParticipantEntity participant = participantRepository.getByIdFullOrThrow(participantId);
        Preconditions.checkArgument(participant.getIsRegistered(), "Участник не закончил регистрацию");
        MilestoneEntity milestone = milestoneRepository.getByIdOrThrow(milestoneId);
        assignParticipantToMilestone(participant, milestone);
        return participantDtoMapper.toDto(participant);
    }

    private void assignParticipantToMilestone(ParticipantEntity participant, MilestoneEntity milestone) {
        Preconditions.checkArgument(participant.getActivity().getId().equals(milestone.getActivity().getId()),
                "Участник %s не может быть привязан к этапу %s, т.к. этап находится в другой активности", participant.getId(), milestone.getId());

        Preconditions.checkState(!Set.of(MilestoneState.SUMMARIZING, MilestoneState.COMPLETED, MilestoneState.SKIPPED).contains(milestone.getState()),
                "Нельзя привязать участника к этапу т.к. этап в состоянии %s", milestone.getState());
        participant.getMilestones().add(milestone);
        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public ParticipantDto removeParticipantFromMilestone(Long participantId, Long milestoneId) {
        ParticipantEntity participant = participantRepository.getByIdOrThrow(participantId);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);

        Set<Long> assignedRounds = participant.getRounds().stream()
                .filter(r -> r.getMilestone().getId().equals(milestoneId))
                .map(RoundEntity::getId)
                .collect(Collectors.toSet());
        Preconditions.checkArgument(assignedRounds.isEmpty(), "Сначала отвяжите участника от раундов этапа: %s", assignedRounds);

        Preconditions.checkState(!Set.of(MilestoneState.SUMMARIZING, MilestoneState.COMPLETED).contains(milestone.getState()),
                "Нельзя отвязать участника от этапа т.к. этап в состоянии %s", milestone.getState());
        participant.getMilestones().remove(milestone);
        participantRepository.save(participant);
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDto> getByRoundByRoundIdForCurrentUser(Long roundId) {
        log.info("Получение участников раунда={} для текущего пользователя={}",
                roundId, currentUser.getSecurityUser().getId());

        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        RoundEntity round = roundRepository.getByIdWithUserOrThrow(roundId);
        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                round.getMilestone().getActivity(), userId, uaa -> uaa.getUser().getId().equals(userId));

        log.debug("Найдено назначение пользователя={} для раунда={}, сторона={}",
                userId, roundId, activityUser.getPartnerSide());

        List<ParticipantEntity> participants = participantRepository.findByRoundId(roundId).stream()
                .filter(p -> {
                    if (activityUser.getPartnerSide() != null) {
                        boolean matches = p.getPartnerSide() == activityUser.getPartnerSide();
                        log.debug("Участник={} со стороной={} соответствует стороне судьи={}: {}",
                                p.getId(), p.getPartnerSide(), activityUser.getPartnerSide(), matches);
                        return matches;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(p -> p.getNumber())).toList();

        log.debug("Найдено {} участников для раунда={} после фильтрации", participants.size(), roundId);

        return participants.stream()
                .map(p -> participantDtoMapper.toDto(p))
                .toList();
    }

    @Override
    public List<RoundParticipantsDto> getByRoundByMilestoneIdForCurrentUser(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                milestone.getActivity(), userId, uaa -> uaa.getUser().getId().equals(userId));

        return milestone.getRounds()
                .stream()
                .sorted(Comparator.comparing(RoundEntity::getRoundOrder))
                .map(re -> {
                    List<ParticipantEntity> participants = re.getParticipants().stream()
                            .filter(p -> {
                                if (activityUser.getPartnerSide() != null) {
                                    return p.getPartnerSide() == activityUser.getPartnerSide();
                                }
                                return true;
                            })
                            .sorted(Comparator.comparing(p -> p.getNumber())).toList();
                    return roundParticipantsDtoMapper.toDto(re, participants);
                }).toList();
    }
}
