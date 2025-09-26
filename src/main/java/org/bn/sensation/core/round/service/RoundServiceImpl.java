package org.bn.sensation.core.round.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bn.sensation.core.common.entity.State;
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

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final RoundDtoMapper roundDtoMapper;
    private final CreateRoundRequestMapper createRoundRequestMapper;
    private final UpdateRoundRequestMapper updateRoundRequestMapper;
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
        // Проверяем существование этапа
        MilestoneEntity milestone = findMilestoneById(request.getMilestoneId());

        // Создаем сущность раунда
        RoundEntity round = createRoundRequestMapper.toEntity(request);
        round.setMilestone(milestone);

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoundDto update(Long id, UpdateRoundRequest request) {
        RoundEntity round = roundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + id));

        if(request.getName()!=null) {
            Preconditions.checkArgument(!request.getName().trim().isEmpty(), "Название раунда не может быть пустым");
        }

        // Обновляем поля раунда
        updateRoundRequestMapper.updateRoundFromRequest(request, round);

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

    private MilestoneEntity findMilestoneById(Long milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + milestoneId));
    }

    private Set<ParticipantEntity> findParticipantsByIds(Set<Long> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<ParticipantEntity> participants = new HashSet<>();
        for (Long participantId : participantIds) {
            ParticipantEntity participant = participantRepository.findById(participantId)
                    .orElseThrow(() -> new EntityNotFoundException("Участник не найден с id: " + participantId));
            participants.add(participant);
        }
        return participants;
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
        return roundRepository.findByMilestoneIdAndStateIn(id, State.LIFE_STATES).stream()
                .map(roundDtoMapper::toDto)
                .toList();
    }
}
