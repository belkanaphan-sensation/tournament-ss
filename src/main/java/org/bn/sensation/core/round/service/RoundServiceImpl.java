package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundRequest;
import org.bn.sensation.core.round.service.dto.RoundDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundRequest;
import org.bn.sensation.core.round.service.mapper.RoundDtoMapper;
import org.bn.sensation.core.round.service.mapper.CreateRoundRequestMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundRequestMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        MilestoneEntity milestone = findMilestoneById(request.getMilestoneId());

        // Create round entity
        RoundEntity round = createRoundRequestMapper.toEntity(request);
        round.setMilestone(milestone);

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoundDto update(Long id, UpdateRoundRequest request) {
        RoundEntity round = roundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Round not found with id: " + id));

        // Update round fields
        updateRoundRequestMapper.updateRoundFromRequest(request, round);

        // Update milestone
        if (request.getMilestoneId() != null) {
            MilestoneEntity milestone = findMilestoneById(request.getMilestoneId());
            round.setMilestone(milestone);
        }

        RoundEntity saved = roundRepository.save(round);
        return roundDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!roundRepository.existsById(id)) {
            throw new EntityNotFoundException("Round not found with id: " + id);
        }
        roundRepository.deleteById(id);
    }

    private MilestoneEntity findMilestoneById(Long milestoneId) {
        if (milestoneId == null) {
            return null;
        }
        return milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found with id: " + milestoneId));
    }
}
