package org.bn.sensation.core.participant.service.mapper;

import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.service.dto.CreateParticipantRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateParticipantRequestMapper extends BaseDtoMapper<ParticipantEntity, CreateParticipantRequest> {
    @Override
    @Mapping(target = "rounds", ignore = true)
    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    ParticipantEntity toEntity(CreateParticipantRequest dto);


}
