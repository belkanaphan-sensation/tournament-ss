package org.bn.sensation.core.milestoneresult.service.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.entity.MilestoneRoundResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneResultDto;
import org.bn.sensation.core.milestoneresult.service.dto.MilestoneRoundResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class, MilestoneRoundResultDtoMapper.class})
public interface MilestoneResultDtoMapper extends BaseDtoMapper<MilestoneResultEntity, MilestoneResultDto> {

    MilestoneRoundResultDtoMapper milestoneRoundResultDtoMapper =
            org.mapstruct.factory.Mappers.getMapper(MilestoneRoundResultDtoMapper.class);

    @Override
    MilestoneResultEntity toEntity(MilestoneResultDto dto);

    @Override
    @Mapping(target = "milestoneRoundResults", source = "roundResults", qualifiedByName = "sortAndMapRoundResults")
    MilestoneResultDto toDto(MilestoneResultEntity entity);

    @org.mapstruct.Named("sortAndMapRoundResults")
    default List<MilestoneRoundResultDto> sortAndMapRoundResults(
            Set<MilestoneRoundResultEntity> roundResults) {
        if (roundResults == null || roundResults.isEmpty()) {
            return null;
        }
        return roundResults.stream()
                .sorted(Comparator.comparing(mrr ->
                        mrr.getRound().getRoundOrder()))
                .map(milestoneRoundResultDtoMapper::toDto)
                .collect(Collectors.toList());
    }
}
