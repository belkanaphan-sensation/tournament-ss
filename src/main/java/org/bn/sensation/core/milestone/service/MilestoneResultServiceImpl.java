package org.bn.sensation.core.milestone.service;

import java.util.Comparator;
import java.util.List;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestone.service.mapper.MilestoneResultDtoMapper;
import org.bn.sensation.core.participant.entity.ParticipantRoundResultEntity;
import org.bn.sensation.core.participant.repository.ParticipantRoundResultRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneResultServiceImpl implements MilestoneResultService{

    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    private final ParticipantRoundResultRepository participantRoundResultRepository;
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
        List<ParticipantRoundResultEntity> roundResults = participantRoundResultRepository.findByMilestoneId(milestoneId);
        List<ParticipantRoundResultEntity> extraRounds = roundResults.stream()
                .filter(prr -> prr.getRound().getExtraRound())
                .sorted(Comparator.comparingLong((ParticipantRoundResultEntity prr) -> prr.getRound().getId()).reversed())
                .toList();
        return List.of();
    }

    @Override
    public void update(MilestoneResultDto milestoneResultDto) {

    }
}
