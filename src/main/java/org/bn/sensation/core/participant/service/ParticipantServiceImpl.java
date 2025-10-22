package org.bn.sensation.core.participant.service;

import java.util.Comparator;
import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
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
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
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
    @Transactional
    public ParticipantDto create(CreateParticipantRequest request) {
        ActivityEntity activity = activityRepository.getByIdOrThrow(request.getActivityId());

        ParticipantEntity participant = createParticipantRequestMapper.toEntity(request);
        participant.setActivity(activity);

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(participantRepository.getByIdFullOrThrow(saved.getId()));
    }

    @Override
    @Transactional
    public ParticipantDto update(Long id, UpdateParticipantRequest request) {
        ParticipantEntity participant = participantRepository.getByIdOrThrow(id);

        updateParticipantRequestMapper.updateParticipantFromRequest(request, participant);

        participantRepository.save(participant);
        return participantDtoMapper.toDto(participantRepository.getByIdFullOrThrow(id));
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
        RoundEntity round = roundRepository.getByIdOrThrow(roundId);
        Preconditions.checkArgument(participant.getActivity().getId().equals(round.getMilestone().getActivity().getId()),
                "Участник %s не может быть привязан к раунду %s, т.к. раунд находится в другой активности", participantId, roundId);

        //TODO проверить стейты в которых может происходить привязка и отвязка
        participant.getRounds().add(round);
        participantRepository.save(participant);
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional
    public ParticipantDto removeParticipantFromRound(Long participantId, Long roundId) {
        ParticipantEntity participant = participantRepository.getByIdOrThrow(participantId);
        RoundEntity round = roundRepository.getByIdOrThrow(roundId);

        //TODO проверить стейты в которых может происходить привязка и отвязка
        participant.getRounds().remove(round);
        participantRepository.save(participant);
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional
    public ParticipantDto assignParticipantToMilestone(Long participantId, Long milestoneId) {
        ParticipantEntity participant = participantRepository.getByIdFullOrThrow(participantId);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        Preconditions.checkArgument(participant.getActivity().getId().equals(milestone.getActivity().getId()),
                "Участник %s не может быть привязан к этапу %s, т.к. этап находится в другой активности", participantId, milestoneId);

        //TODO проверить стейты в которых может происходить привязка и отвязка
        participant.getMilestones().add(milestone);
        participantRepository.save(participant);
        return participantDtoMapper.toDto(participant);
    }

    @Override
    @Transactional
    public ParticipantDto removeParticipantFromMilestone(Long participantId, Long milestoneId) {
        ParticipantEntity participant = participantRepository.getByIdOrThrow(participantId);
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);

        //TODO проверить стейты в которых может происходить привязка и отвязка
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
