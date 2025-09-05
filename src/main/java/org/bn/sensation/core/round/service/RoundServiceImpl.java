package org.bn.sensation.core.round.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final RoundDtoMapper roundDtoMapper;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;

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
        // Validate milestone exists
        MilestoneEntity milestone = null;
        if (request.getMilestoneId() != null) {
            milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new IllegalArgumentException("Milestone not found with id: " + request.getMilestoneId()));
        }

        // Get participants
        Set<ParticipantEntity> participants = Set.of();
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            participants = request.getParticipantIds().stream()
                    .map(participantId -> participantRepository.findById(participantId)
                            .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + participantId)))
                    .collect(Collectors.toSet());
        }

        // Create round entity
        RoundEntity round = RoundEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .milestone(milestone)
                .participants(participants)
                .build();

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoundDto update(Long id, UpdateRoundRequest request) {
        RoundEntity round = roundRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Round not found with id: " + id));

        // Update round fields
        if (request.getName() != null) round.setName(request.getName());
        if (request.getDescription() != null) round.setDescription(request.getDescription());

        // Update milestone
        if (request.getMilestoneId() != null) {
            MilestoneEntity milestone = milestoneRepository.findById(request.getMilestoneId())
                    .orElseThrow(() -> new IllegalArgumentException("Milestone not found with id: " + request.getMilestoneId()));
            round.setMilestone(milestone);
        }

        // Update participants
        if (request.getParticipantIds() != null) {
            Set<ParticipantEntity> participants = request.getParticipantIds().stream()
                    .map(participantId -> participantRepository.findById(participantId)
                            .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + participantId)))
                    .collect(Collectors.toSet());
            round.setParticipants(participants);
        }

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!roundRepository.existsById(id)) {
            throw new IllegalArgumentException("Round not found with id: " + id);
        }
        roundRepository.deleteById(id);
    }
}
