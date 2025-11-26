package org.bn.sensation.core.milestoneresult.service.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneRoundResultDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class, MilestoneRoundResultDtoMapper.class})
public interface MilestoneResultDtoMapper extends BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> {

    @Override
    MilestoneResultEntity toEntity(MilestoneResultDto dto);

    @Override
    @Mapping(target = "milestoneRoundResults", source = "roundResults")
    MilestoneResultDto toDto(MilestoneResultEntity entity);

    @AfterMapping
    default void sortRoundResultsByRoundOrder(@MappingTarget MilestoneResultDto dto) {
        if (dto.getMilestoneRoundResults() != null) {
            List<MilestoneRoundResultDto> sortedResults = dto.getMilestoneRoundResults().stream()
                    .sorted(Comparator.comparing(MilestoneRoundResultDto::getRoundOrder))
                    .collect(Collectors.toList());
            dto.setMilestoneRoundResults(sortedResults);
        }
    }
}
