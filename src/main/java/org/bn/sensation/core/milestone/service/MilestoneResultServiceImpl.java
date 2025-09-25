package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestone.repository.MilestoneResultRepository;
import org.bn.sensation.core.milestone.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestone.service.mapper.MilestoneResultDtoMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneResultServiceImpl implements MilestoneResultService{

    private final MilestoneResultRepository milestoneResultRepository;
    private final MilestoneResultDtoMapper milestoneResultDtoMapper;
    @Override
    public BaseRepository<MilestoneResultEntity> getRepository() {
        return milestoneResultRepository;
    }

    @Override
    public BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> getMapper() {
        return milestoneResultDtoMapper;
    }
}
