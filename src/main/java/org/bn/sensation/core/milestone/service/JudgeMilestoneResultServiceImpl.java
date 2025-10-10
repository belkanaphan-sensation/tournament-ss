package org.bn.sensation.core.milestone.service;

import java.util.List;
import java.util.Objects;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.criteria.repository.MilestoneCriteriaAssignmentRepository;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultRoundRequest;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.core.milestone.repository.JudgeMilestoneResultRepository;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.JudgeMilestoneResultMilestoneRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateJudgeMilestoneResultRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.JudgeMilestoneResultDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateJudgeMilestoneResultRequestMapper;
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
public class JudgeMilestoneResultServiceImpl implements JudgeMilestoneResultService {

    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    private final JudgeMilestoneResultDtoMapper judgeMilestoneResultDtoMapper;
    private final CreateJudgeMilestoneResultRequestMapper createJudgeMilestoneResultRequestMapper;
    private final UpdateJudgeMilestoneResultRequestMapper updateJudgeMilestoneResultRequestMapper;
    private final MilestoneCriteriaAssignmentRepository milestoneCriteriaAssignmentRepository;
    private final UserActivityAssignmentRepository userActivityAssignmentRepository;
    private final RoundRepository roundRepository;
    private final ParticipantRepository participantRepository;
    private final CurrentUser currentUser;

    @Override
    public BaseRepository<JudgeMilestoneResultEntity> getRepository() {
        return judgeMilestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<JudgeMilestoneResultEntity, JudgeMilestoneResultDto> getMapper() {
        return judgeMilestoneResultDtoMapper;
    }

    @Override
    @Transactional
    //todo тут должно быть применено правило, если судьи меняются сторонами, пока оно не учитывается
    public JudgeMilestoneResultDto create(JudgeMilestoneResultRoundRequest request) {
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

        boolean exists = judgeMilestoneResultRepository.existsByRoundIdAndParticipantIdAndActivityUserIdAndMilestoneCriteriaId(
                request.getRoundId(), request.getParticipantId(), activityUser.getId(), request.getMilestoneCriteriaId());
        Preconditions.checkArgument(!exists, "Результат уже существует для данного раунда, участника, судьи и критерия");

        JudgeMilestoneResultEntity entity = createJudgeMilestoneResultRequestMapper.toEntity(request);
        entity.setParticipant(participant);
        entity.setRound(roundEntity);
        entity.setActivityUser(activityUser);
        entity.setMilestoneCriteria(milestoneCriteria);
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);
        return judgeMilestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public JudgeMilestoneResultDto update(Long id, JudgeMilestoneResultMilestoneRequest request) {
        JudgeMilestoneResultEntity entity = judgeMilestoneResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат раунда не найден с id: " + id));

        Preconditions.checkArgument(currentUser.getSecurityUser()
                        .getRoles()
                        .stream()
                        .anyMatch(role -> role == Role.ADMIN || role == Role.SUPERADMIN || role == Role.OCCASION_ADMIN)
                        || entity.getActivityUser().getUser().getId().equals(currentUser.getSecurityUser().getId()),
                "Нельзя изменить результат");
        updateJudgeMilestoneResultRequestMapper.updateRoundFromRequest(request, entity);
        JudgeMilestoneResultEntity saved = judgeMilestoneResultRepository.save(entity);
        return judgeMilestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByRoundId(Long roundId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByRoundId(roundId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByMilestoneId(Long milestoneId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByMilestoneId(milestoneId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByParticipantId(Long participantId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByParticipantId(participantId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgeMilestoneResultDto> findByActivityUserId(Long activityUserId) {
        List<JudgeMilestoneResultEntity> entities = judgeMilestoneResultRepository.findByActivityUserId(activityUserId);
        return entities.stream()
                .map(judgeMilestoneResultDtoMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JudgeMilestoneResultDto> findAll(Pageable pageable) {
        return judgeMilestoneResultRepository.findAll(pageable).map(judgeMilestoneResultDtoMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!judgeMilestoneResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Результат раунда не найден с id: " + id);
        }

        judgeMilestoneResultRepository.deleteById(id);
    }

}
