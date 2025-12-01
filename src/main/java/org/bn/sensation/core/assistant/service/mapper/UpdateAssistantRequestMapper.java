package org.bn.sensation.core.assistant.service.mapper;

import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.assistant.service.dto.UpdateAssistantRequest;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.mapstruct.*;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateAssistantRequestMapper extends BaseDtoMapper<AssistantEntity, UpdateAssistantRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "person.name", source = "name")
    @Mapping(target = "person.surname", source = "surname")
    @Mapping(target = "person.secondName", source = "secondName")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phoneNumber", source = "phoneNumber")
    void updateParticipantFromRequest(UpdateAssistantRequest request, @MappingTarget AssistantEntity entity);

}
