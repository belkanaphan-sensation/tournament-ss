package org.bn.sensation.core.round.service;

import java.util.List;
import java.util.Objects;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.round.repository.RoundResultRepository;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.bn.sensation.core.round.service.dto.RoundResultDto;
import org.bn.sensation.core.round.service.dto.UpdateRoundResultRequest;
import org.bn.sensation.core.round.service.mapper.CreateRoundResultRequestMapper;
import org.bn.sensation.core.round.service.mapper.RoundResultDtoMapper;
import org.bn.sensation.core.round.service.mapper.UpdateRoundResultRequestMapper;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.bn.sensation.core.user.repository.UserActivityAssignmentRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoundResultServiceImpl implements RoundResultService {

    private final RoundResultRepository roundResultRepository;
    private final RoundResultDtoMapper roundResultDtoMapper;
    private final CreateRoundResultRequestMapper createRoundResultRequestMapper;
    private final UpdateRoundResultRequestMapper updateRoundResultRequestMapper;
    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final RoundRepository roundRepository;
    private final ParticipantRepository participantRepository;
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
    //todo тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
    public RoundResultDto create(CreateRoundResultRequest request) {
        ParticipantEntity participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(EntityNotFoundException::new);
        MilestoneCriteriaAssignmentEntity milestoneCriteria = milestoneCriteriaAssignmentRepository
                .findById(request.getMilestoneCriteriaId())
                .orElseThrow(EntityNotFoundException::new);
        RoundEntity roundEntity = roundRepository.findById(request.getRoundId())
                .orElseThrow(EntityNotFoundException::new);
        UserActivityAssignmentEntity activityUser = userActivityAssignmentRepository.findByUserIdAndActivityId(
                        currentUser.getSecurityUser().getId(),
                        roundEntity.getMilestone().getActivity().getId())
                .orElseThrow(EntityNotFoundException::new);

        Preconditions.checkArgument(activityUser.getPosition().isJudge(), "Оценивающий должен быть судьей");

        Preconditions.checkArgument(milestoneCriteria.getPartnerSide() == null
                        || activityUser.getPartnerSide() == null
                        || Objects.equals(milestoneCriteria.getPartnerSide(), activityUser.getPartnerSide()),
                "Сторона судьи и критерия не совпадает");

        Preconditions.checkArgument(roundEntity.getParticipants().stream()
                        .anyMatch(p -> p.getId().equals(request.getParticipantId())),
                "Участник не участвует в данном раунде");

        boolean exists = roundResultRepository.existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
                request.getRoundId(), request.getParticipantId(), activityUser.getId(), request.getMilestoneCriteriaId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        RoundResultEntity entity = createRoundResultRequestMapper.toEntity(request);
        entity.setParticipant(participant);
        entity.setRound(roundEntity);
        entity.setActivityUser(activityUser);
        entity.setMilestoneCriteria(milestoneCriteria);
        RoundResultEntity saved = roundResultRepository.save(entity);
        return roundResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public RoundResultDto update(Long id, UpdateRoundResultRequest request) {
        RoundResultEntity entity = roundResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден с id: " + id));

        Preconditions.checkArgument(currentUser.getSecurityUser()
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role == Role.ADMIN || role == Role.SUPERADMIN || role == Role.OCCASION_ADMIN)
                        || entity.getActivityUser().getUser().getId().equals(currentUser.getSecurityUser().getId()),
                "Нельзя изменить результат");
        updateRoundResultRequestMapper.updateRoundFromRequest(request, entity);
        RoundResultEntity saved = roundResultRepository.save(entity);
        return roundResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResultDto> findByRoundId(Long roundId) {
        List<RoundResultEntity> entities = roundResultRepository.findByRoundId(roundId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResultDto> findByMilestoneId(Long milestoneId) {
        List<RoundResultEntity> entities = roundResultRepository.findByMilestoneId(milestoneId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResultDto> findByParticipantId(Long participantId) {
        List<RoundResultEntity> entities = roundResultRepository.findByParticipantId(participantId);
        return entities.stream()
                .map(roundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
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
