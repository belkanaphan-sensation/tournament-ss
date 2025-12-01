package org.bn.sensation.core.assistant.service;

import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.assistant.service.dto.AssistantDto;
import org.bn.sensation.core.assistant.service.dto.CreateAssistantRequest;
import org.bn.sensation.core.assistant.service.dto.UpdateAssistantRequest;
import org.bn.sensation.core.common.service.BaseCrudService;

public interface AssistantService extends BaseCrudService<
        AssistantEntity,
        AssistantDto,
        CreateAssistantRequest,
        UpdateAssistantRequest> {
}
