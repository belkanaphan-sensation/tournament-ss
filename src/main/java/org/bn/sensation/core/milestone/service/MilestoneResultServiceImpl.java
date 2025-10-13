package org.bn.sensation.core.milestone.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.judge.entity.JudgeMilestoneEntity;
import org.bn.sensation.core.judge.entity.JudgeMilestoneStatus;
import org.bn.sensation.core.judge.repository.JudgeMilestoneRepository;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestone.service.dto.CreateMilestoneResultRequest;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestone.service.dto.UpdateMilestoneResultRequest;
import org.bn.sensation.core.milestone.service.mapper.CreateMilestoneResultRequestMapper;
import org.bn.sensation.core.milestone.service.mapper.MilestoneResultDtoMapper;
import org.bn.sensation.core.milestone.service.mapper.UpdateMilestoneResultRequestMapper;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
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
public class MilestoneResultServiceImpl implements MilestoneResultService {

    private final JudgeMilestoneRepository judgeMilestoneRepository;
    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    private final CreateMilestoneResultRequestMapper createMilestoneResultRequestMapper;
    private final UpdateMilestoneResultRequestMapper updateMilestoneResultRequestMapper;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;

    @Override
    public BaseRepository<MilestoneResultEntity> getRepository() {
        return milestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> getMapper() {
        return milestoneResultDtoMapper;
    }

    @Override
    @Transactional
    //TODO вопрос про то, как и где должен админ обновлять поля. Например принимать решение по местам
    public List<MilestoneResultDto> getByMilestoneId(Long milestoneId) {
        Preconditions.checkArgument(milestoneId != null, "ID этапа не может быть null");
        MilestoneEntity milestone = milestoneRepository.findByIdFullEntity(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + milestoneId));
        Map<Long, JudgeMilestoneEntity> judgeResults = judgeMilestoneRepository.findByMilestoneId(milestoneId)
                .stream()
                .collect(Collectors.toMap(jm -> jm.getJudge().getId(), Function.identity()));
        Preconditions.checkState(milestone.getActivity().getUserAssignments().stream()
                .allMatch(ua -> judgeResults.get(ua.getId()) != null
                        && judgeResults.get(ua.getId()).getStatus() == JudgeMilestoneStatus.READY),
                "Для получения результатов этапа все судьи должны завершить этап");
        if (milestone.getResults().isEmpty()) {
            //создаем результаты из всего что есть
        } else {
            //пересчитываем
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MilestoneResultDto> findAll(Pageable pageable) {
        return milestoneResultRepository.findAll(pageable).map(milestoneResultDtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MilestoneResultDto> findById(Long id) {
        return milestoneResultRepository.findById(id)
                .map(milestoneResultDtoMapper::toDto);
    }

    @Override
    @Transactional
    public MilestoneResultDto create(CreateMilestoneResultRequest request) {
        Preconditions.checkArgument(request.getMilestoneId() != null, "Milestone ID не может быть null");
        Preconditions.checkArgument(request.getParticipantId() != null, "Participant ID не может быть null");
        Preconditions.checkArgument(request.getRoundId() != null, "Round ID не может быть null");

        MilestoneEntity milestone = milestoneRepository.findById(request.getMilestoneId())
                .orElseThrow(() -> new EntityNotFoundException("Этап не найден с id: " + request.getMilestoneId()));

        ParticipantEntity participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new EntityNotFoundException("Участник не найден с id: " + request.getParticipantId()));

        RoundEntity round = roundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("Раунд не найден с id: " + request.getRoundId()));

        MilestoneResultEntity result = createMilestoneResultRequestMapper.toEntity(request);
        result.setMilestone(milestone);
        result.setParticipant(participant);
//        result.setRound(round);

        MilestoneResultEntity saved = milestoneResultRepository.save(result);
        return milestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public MilestoneResultDto update(Long id, UpdateMilestoneResultRequest request) {
        Preconditions.checkArgument(id != null, "ID результата не может быть null");

        MilestoneResultEntity result = milestoneResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Результат этапа не найден с id: " + id));

        updateMilestoneResultRequestMapper.updateMilestoneResultFromRequest(request, result);

        MilestoneResultEntity saved = milestoneResultRepository.save(result);
        return milestoneResultDtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Preconditions.checkArgument(id != null, "ID результата не может быть null");
        if (!milestoneResultRepository.existsById(id)) {
            throw new EntityNotFoundException("Результат этапа не найден с id: " + id);
        }
        milestoneResultRepository.deleteById(id);
    }

}
