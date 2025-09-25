package org.bn.sensation.core.round.service;

import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.repository.RoundResultRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundResultRequest;
import org.bn.sensation.core.round.service.mapper.CreateRoundResultRequestMapper;
import org.bn.sensation.core.round.service.mapper.RoundResultDtoMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundResultRequestMapper;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoundResultServiceImpl implements RoundResultService {

    private final RoundResultRepository roundResultRepository;
    private final RoundResultDtoMapper roundResultDtoMapper;
    private final CreateRoundResultRequestMapper createRoundResultRequestMapper;
    private final UpdateRoundResultRequestMapper updateRoundResultRequestMapper;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<RoundResultEntity> getRepository() {
        return roundResultRepository;
    }

    @Override
    public BaseDtoMapper<RoundResultEntity, RoundResultDto> getMapper() {
        return roundResultDtoMapper;
    }

    @Override
    @Transactional
    public RoundResultDto create(CreateRoundResultRequest request) {
        RoundResultEntity entity = createRoundResultRequestMapper.toEntity(request);
        RoundResultEntity saved = roundResultRepository.save(entity);
        return roundResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoundResultDto update(Long id, UpdateRoundResultRequest request) {
        RoundResultEntity entity = roundResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден с id: " + id));

        updateRoundResultRequestMapper.updateRoundFromRequest(request, entity);
        RoundResultEntity saved = roundResultRepository.save(entity);
        return roundResultDtoMapper.toDto(saved);
    }

    @Override
    public List<RoundResultDto> findByRoundId(Long roundId) {
        List<RoundResultEntity> entities = roundResultRepository.findByRoundId(roundId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    public List<RoundResultDto> findByMilestoneId(Long milestoneId) {
        List<RoundResultEntity> entities = roundResultRepository.findByMilestoneId(milestoneId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    public List<RoundResultDto> findByParticipantId(Long participantId) {
        List<RoundResultEntity> entities = roundResultRepository.findByParticipantId(participantId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    public List<RoundResultDto> findByActivityUserId(Long activityUserId) {
        List<RoundResultEntity> entities = roundResultRepository.findByActivityUserId(activityUserId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoundResultDto> findAll(Pageable pageable) {
        return roundResultRepository.findAll(pageable).map(roundResultDtoMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!roundResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Результат раунда не найден с id: " + id);
        }

        roundResultRepository.deleteById(id);
    }

}
