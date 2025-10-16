package org.bn.sensation.core.participant.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
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
    private final EntityLinkMapper entityLinkMapper;
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
                .map(this::enrichParticipantDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipantDto create(CreateParticipantRequest request) {
        ActivityEntity activity = activityRepository.findById(request.getActivityId())
                .orElseThrow(() -> new EntityNotFoundException("Активность не найдена с id: " + request.getActivityId()));

        ParticipantEntity participant = createParticipantRequestMapper.toEntity(request);
        participant.setActivity(activity);

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipantDto update(Long id, UpdateParticipantRequest request) {
        ParticipantEntity participant = findParticipantById(id);

        updateParticipantRequestMapper.updateParticipantFromRequest(request, participant);

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(saved);
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
        ParticipantEntity participant = findParticipantById(participantId);
        RoundEntity round = findRoundById(roundId);

        participant.getRounds().add(round);
        ParticipantEntity saved = participantRepository.save(participant);
        return enrichParticipantDto(saved);
    }

    @Override
    @Transactional
    public ParticipantDto removeParticipantFromRound(Long participantId, Long roundId) {
        ParticipantEntity participant = findParticipantById(participantId);
        RoundEntity round = findRoundById(roundId);

        participant.getRounds().remove(round);
        ParticipantEntity saved = participantRepository.save(participant);
        return enrichParticipantDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantDto> getByRoundByRoundIdForCurrentUser(Long roundId) {
        log.info("Получение участников раунда={} для текущего пользователя={}", 
                roundId, currentUser.getSecurityUser().getId());
        
        Preconditions.checkArgument(roundId != null, "ID раунда не может быть null");
        RoundEntity round = roundRepository.findByIdWithUserAssignments(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + roundId));
        Long userId = currentUser.getSecurityUser().getId();
        UserActivityAssignmentEntity activityAssignment = round.getMilestone()
                .getActivity()
                .getUserAssignments()
                .stream()
                .filter(uaa -> uaa.getUser().getId().equals(userId))
                .findFirst().orElseThrow(EntityNotFoundException::new);
        
        log.debug("Найдено назначение пользователя={} для раунда={}, сторона={}", 
                userId, roundId, activityAssignment.getPartnerSide());
        
        List<ParticipantEntity> participants = participantRepository.findByRoundId(roundId).stream()
                .filter(p -> {
                    if (activityAssignment.getPartnerSide() != null) {
                        boolean matches = p.getPartnerSide() == activityAssignment.getPartnerSide();
                        log.debug("Участник={} со стороной={} соответствует стороне судьи={}: {}", 
                                p.getId(), p.getPartnerSide(), activityAssignment.getPartnerSide(), matches);
                        return matches;
                    }
                    return true;
                })
                .sorted(Comparator.comparing(p -> p.getNumber())).toList();
        
        log.debug("Найдено {} участников для раунда={} после фильтрации", participants.size(), roundId);
        
        return participants.stream()
                .map(this::enrichParticipantDto)
                .toList();
    }

    @Override
    public List<RoundParticipantsDto> getByRoundByMilestoneIdForCurrentUser(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + milestoneId));
        Long userId = currentUser.getSecurityUser().getId();
        UserActivityAssignmentEntity activityAssignment = milestone.getActivity()
                .getUserAssignments()
                .stream()
                .filter(uaa -> uaa.getUser().getId().equals(userId))
                .findFirst().orElseThrow(EntityNotFoundException::new);
        return milestone.getRounds()
                .stream()
                .sorted(Comparator.comparing(re -> re.getId()))
                .map(re -> {
                    List<ParticipantEntity> participants = re.getParticipants().stream()
                            .filter(p -> {
                                if (activityAssignment.getPartnerSide() != null) {
                                    return p.getPartnerSide() == activityAssignment.getPartnerSide();
                                }
                                return true;
                            })
                            .sorted(Comparator.comparing(p -> p.getNumber())).toList();
                    return roundParticipantsDtoMapper.toDto(re, participants);
                }).toList();
    }

    private RoundEntity findRoundById(Long roundId) {
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + roundId));
    }

    private ParticipantEntity findParticipantById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Участник не найден с id: " + participantId));
    }

    /**
     * Обогащает ParticipantDto недостающими данными (activity и milestones)
     */
    private ParticipantDto enrichParticipantDto(ParticipantEntity participant) {
        log.debug("Обогащение данных участника={}", participant.getId());
        
        ParticipantDto dto = participantDtoMapper.toDto(participant);

        Set<EntityLinkDto> activities = participant.getRounds().stream()
                .map(RoundEntity::getMilestone)
                .map(MilestoneEntity::getActivity)
                .map(entityLinkMapper::toEntityLinkDto).collect(Collectors.toSet());
        
        log.debug("Найдено {} активностей для участника={}", activities.size(), participant.getId());
        
        Preconditions.checkArgument(activities.size() == 1, "Неконсистентное состояние, " +
                "участник находится в нескольких активностях: " + activities);
        // Устанавливаем активность из раундов
        dto.setActivity(activities.iterator().next());

        // Устанавливаем этапы из раундов
        Set<EntityLinkDto> milestones = participant.getRounds().stream()
                .map(RoundEntity::getMilestone)
                .map(entityLinkMapper::toEntityLinkDto)
                .collect(Collectors.toSet());
        
        log.debug("Найдено {} этапов для участника={}", milestones.size(), participant.getId());
        dto.setMilestones(milestones);

        return dto;
    }
}
