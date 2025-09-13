package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = BaseDtoMapper.class)
public interface CreateParticipantRequestMapper extends BaseDtoMapper<ParticipantEntity, CreateParticipantRequest> {
    @Override
    @Mapping(target = "activity", source = "activityId")
    @Mapping(target = "rounds", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    ParticipantEntity toEntity(CreateParticipantRequest dto);

    @Override
    @Mapping(target = "name", source = "person.name")
    @Mapping(target = "surname", source = "person.surname")
    @Mapping(target = "secondName", source = "person.secondName")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "phoneNumber", source = "person.phoneNumber")
    @Mapping(target = "activityId", source = "activity.id")
    @Mapping(target = "roundIds", source = "rounds")
    CreateParticipantRequest toDto(ParticipantEntity entity);

    default ActivityEntity map(Long activityId) {
        if (activityId == null) {
            return null;
        }
        ActivityEntity activity = new ActivityEntity();
        activity.setId(activityId);
        return activity;
    }

    default Set<Long> map(Set<RoundEntity> rounds) {
        if (rounds == null) {
            return null;
        }
        return rounds.stream()
                .map(RoundEntity::getId)
                .collect(Collectors.toSet());
    }

}
