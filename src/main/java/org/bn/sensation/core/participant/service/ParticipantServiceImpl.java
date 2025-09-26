package org.bn.sensation.core.participant.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRequest;
import org.bn.sensation.core.participant.service.mapper.CreateParticipantRequestMapper;
import org.bn.sensation.core.participant.service.mapper.ParticipantDtoMapper;
import org.bn.sensation.core.participant.service.mapper.UpdateParticipantRequestMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

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
    private final EntityLinkMapper entityLinkMapper;

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
    public List<ParticipantDto> findByRoundId(Long roundId) {
        return participantRepository.findByRoundId(roundId).stream()
                .map(this::enrichParticipantDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipantDto create(CreateParticipantRequest request) {
        ParticipantEntity participant = createParticipantRequestMapper.toEntity(request);

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
        ParticipantDto dto = participantDtoMapper.toDto(participant);

        Set<EntityLinkDto> activities = participant.getRounds().stream()
                .map(RoundEntity::getMilestone)
                .map(MilestoneEntity::getActivity)
                .map(entityLinkMapper::toEntityLinkDto).collect(Collectors.toSet());
        Preconditions.checkArgument(activities.size() == 1, "Неконсистентное состояние, " +
                "участник находится в нескольких активностях: " + activities);
        // Устанавливаем активность из раундов
        dto.setActivity(activities.iterator().next());

        // Устанавливаем этапы из раундов
        dto.setMilestones(participant.getRounds().stream()
                .map(RoundEntity::getMilestone)
                .map(entityLinkMapper::toEntityLinkDto)
                .collect(Collectors.toSet()));

        return dto;
    }
}
