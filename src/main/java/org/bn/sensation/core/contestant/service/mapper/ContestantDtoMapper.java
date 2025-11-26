package org.bn.sensation.core.contestant.service.mapper;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.mapper.EntityLinkMapper;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.service.dto.ContestParticipantDto;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseDtoMapper.class, uses = {EntityLinkMapper.class, ContestParticipantDtoMapper.class})
public interface ContestantDtoMapper extends BaseDtoMapper<ContestantEntity, ContestantDto> {

    @Override
    ContestantEntity toEntity(ContestantDto dto);

    @Override
    ContestantDto toDto(ContestantEntity entity);

    @AfterMapping
    default void sortParticipantsByPartnerSide(@MappingTarget ContestantDto dto) {
        if (dto.getParticipants() != null) {
            LinkedHashSet<ContestParticipantDto> sortedParticipants = dto.getParticipants().stream()
                    .sorted(Comparator.comparing(ContestParticipantDto::getPartnerSide))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            dto.setParticipants(sortedParticipants);
        }
    }
}
