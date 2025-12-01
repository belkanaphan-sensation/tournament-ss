package org.bn.sensation.core.assistant.service.mapper;

import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.assistant.service.dto.CreateAssistantRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateAssistantRequestMapper extends BaseDtoMapper<AssistantEntity, CreateAssistantRequest> {
    @Override
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    AssistantEntity toEntity(CreateAssistantRequest dto);

}

