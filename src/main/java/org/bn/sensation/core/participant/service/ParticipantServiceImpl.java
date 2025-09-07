package org.bn.sensation.core.participant.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.bn.sensation.core.participant.service.mapper.ParticipantDtoMapper;
import org.bn.sensation.core.participant.service.mapper.CreateParticipantRequestMapper;
import org.bn.sensation.core.participant.service.mapper.UpdateParticipantRequestMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;
    private final ParticipantDtoMapper participantDtoMapper;
    private final CreateParticipantRequestMapper createParticipantRequestMapper;
    private final UpdateParticipantRequestMapper updateParticipantRequestMapper;
    private final ActivityRepository activityRepository;

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
    @Transactional
    public ParticipantDto create(CreateParticipantRequest request) {
        // Validate activity exists
        ActivityEntity activity = findActivityById(request.getActivityId());

        // Get rounds
        Set<RoundEntity> rounds = findRoundsByIds(request.getRoundIds());

        // Create participant entity
        ParticipantEntity participant = createParticipantRequestMapper.toEntity(request);
        participant.setActivity(activity);
        participant.setRounds(rounds);

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipantDto update(Long id, UpdateParticipantRequest request) {
        ParticipantEntity participant = findParticipantById(id);

        // Update participant fields
        updateParticipantRequestMapper.updateParticipantFromRequest(request, participant);

        // Update activity
        if (request.getActivityId() != null) {
            ActivityEntity activity = findActivityById(request.getActivityId());
            participant.setActivity(activity);
        }

        // Update rounds
        if (request.getRoundIds() != null) {
            Set<RoundEntity> rounds = findRoundsByIds(request.getRoundIds());
            participant.setRounds(rounds);
        }

        ParticipantEntity saved = participantRepository.save(participant);
        return participantDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new IllegalArgumentException("Participant not found with id: " + id);
        }
        participantRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ParticipantDto assignParticipantToRound(Long participantId, Long roundId) {
        ParticipantEntity participant = findParticipantById(participantId);
        RoundEntity round = findRoundById(roundId);

        participant.getRounds().add(round);
        return participantDtoMapper.toDto(participantRepository.save(participant));
    }

    private ActivityEntity findActivityById(Long activityId) {
        if (activityId == null) {
            return null;
        }
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found with id: " + activityId));
    }

    private Set<RoundEntity> findRoundsByIds(Set<Long> roundIds) {
        if (roundIds == null || roundIds.isEmpty()) {
            return Set.of();
        }
        return roundIds.stream()
                .map(this::findRoundById)
                .collect(Collectors.toSet());
    }

    private RoundEntity findRoundById(Long roundId) {
        return roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found with id: " + roundId));
    }

    private ParticipantEntity findParticipantById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with id: " + participantId));
    }
}
