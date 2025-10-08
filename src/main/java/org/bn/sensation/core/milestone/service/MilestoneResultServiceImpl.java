package org.bn.sensation.core.milestone.service;

import java.util.Comparator;
import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestone.service.mapper.MilestoneResultDtoMapper;
import org.bn.sensation.core.milestone.entity.JudgeMilestoneResultEntity;
import org.bn.sensation.core.milestone.repository.JudgeMilestoneResultRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneResultServiceImpl implements MilestoneResultService{

    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    private final JudgeMilestoneResultRepository judgeMilestoneResultRepository;
    @Override
    public BaseRepository<MilestoneResultEntity> getRepository() {
        return milestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> getMapper() {
        return milestoneResultDtoMapper;
    }

    @Override
    public List<MilestoneResultDto> getByMilestoneId(Long milestoneId) {
        List<JudgeMilestoneResultEntity> roundResults = judgeMilestoneResultRepository.findByMilestoneId(milestoneId);
        List<JudgeMilestoneResultEntity> extraRounds = roundResults.stream()
                .filter(prr -> prr.getRound().getExtraRound())
                .sorted(Comparator.comparingLong((JudgeMilestoneResultEntity prr) -> prr.getRound().getId()).reversed())
                .toList();
        return List.of();
    }

    @Override
    public void update(MilestoneResultDto milestoneResultDto) {

    }
}
