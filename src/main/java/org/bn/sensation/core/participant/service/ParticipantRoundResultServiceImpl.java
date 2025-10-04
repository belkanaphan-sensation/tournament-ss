package org.bn.sensation.core.participant.service;

import java.util.List;
import java.util.Objects;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.participant.repository.ParticipantRoundResultRepository;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRoundResultRequest;
import org.bn.sensation.core.participant.service.dto.ParticipantRoundResultDto;
import org.bn.sensation.core.participant.service.dto.UpdateParticipantRoundResultRequest;
import org.bn.sensation.core.participant.service.mapper.CreateParticipantRoundResultRequestMapper;
import org.bn.sensation.core.participant.service.mapper.ParticipantRoundResultDtoMapper;
import org.bn.sensation.core.participant.service.mapper.UpdateParticipantRoundResultRequestMapper;
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
public class ParticipantRoundResultServiceImpl implements ParticipantRoundResultService {

    private final ParticipantRoundResultRepository participantRoundResultRepository;
    private final ParticipantRoundResultDtoMapper participantRoundResultDtoMapper;
    private final CreateParticipantRoundResultRequestMapper createParticipantRoundResultRequestMapper;
    private final UpdateParticipantRoundResultRequestMapper updateParticipantRoundResultRequestMapper;
    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final RoundRepository roundRepository;
    private final ParticipantRepository participantRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<ParticipantRoundResultEntity> getRepository() {
        return participantRoundResultRepository;
    }

    @Override
    public BaseDtoMapper<ParticipantRoundResultEntity, ParticipantRoundResultDto> getMapper() {
        return participantRoundResultDtoMapper;
    }

    @Override
    @Transactional
    //todo тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
    public ParticipantRoundResultDto create(CreateParticipantRoundResultRequest request) {
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

        boolean exists = participantRoundResultRepository.existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
                request.getRoundId(), request.getParticipantId(), activityUser.getId(), request.getMilestoneCriteriaId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        ParticipantRoundResultEntity entity = createParticipantRoundResultRequestMapper.toEntity(request);
        entity.setParticipant(participant);
        entity.setRound(roundEntity);
        entity.setActivityUser(activityUser);
        entity.setMilestoneCriteria(milestoneCriteria);
        ParticipantRoundResultEntity saved = participantRoundResultRepository.save(entity);
        return participantRoundResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ParticipantRoundResultDto update(Long id, UpdateParticipantRoundResultRequest request) {
        ParticipantRoundResultEntity entity = participantRoundResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден с id: " + id));

        Preconditions.checkArgument(currentUser.getSecurityUser()
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role == Role.ADMIN || role == Role.SUPERADMIN || role == Role.OCCASION_ADMIN)
                        || entity.getActivityUser().getUser().getId().equals(currentUser.getSecurityUser().getId()),
                "Нельзя изменить результат");
        updateParticipantRoundResultRequestMapper.updateRoundFromRequest(request, entity);
        ParticipantRoundResultEntity saved = participantRoundResultRepository.save(entity);
        return participantRoundResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantRoundResultDto> findByRoundId(Long roundId) {
        List<ParticipantRoundResultEntity> entities = participantRoundResultRepository.findByRoundId(roundId);
        return entities.stream()
                .map(participantRoundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantRoundResultDto> findByMilestoneId(Long milestoneId) {
        List<ParticipantRoundResultEntity> entities = participantRoundResultRepository.findByMilestoneId(milestoneId);
        return entities.stream()
                .map(participantRoundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantRoundResultDto> findByParticipantId(Long participantId) {
        List<ParticipantRoundResultEntity> entities = participantRoundResultRepository.findByParticipantId(participantId);
        return entities.stream()
                .map(participantRoundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantRoundResultDto> findByActivityUserId(Long activityUserId) {
        List<ParticipantRoundResultEntity> entities = participantRoundResultRepository.findByActivityUserId(activityUserId);
        return entities.stream()
                .map(participantRoundResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ParticipantRoundResultDto> findAll(Pageable pageable) {
        return participantRoundResultRepository.findAll(pageable).map(participantRoundResultDtoMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!participantRoundResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Результат раунда не найден с id: " + id);
        }

        participantRoundResultRepository.deleteById(id);
    }

}
