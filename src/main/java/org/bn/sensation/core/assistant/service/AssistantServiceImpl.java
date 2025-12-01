package org.bn.sensation.core.assistant.service;

import org.bn.sensation.core.assistant.entity.AssistantEntity;
import org.bn.sensation.core.assistant.repository.AssistantRepository;
import org.bn.sensation.core.assistant.service.dto.AssistantDto;
import org.bn.sensation.core.assistant.service.dto.CreateAssistantRequest;
import org.bn.sensation.core.assistant.service.dto.UpdateAssistantRequest;
import org.bn.sensation.core.assistant.service.mapper.AssistantDtoMapper;
import org.bn.sensation.core.assistant.service.mapper.CreateAssistantRequestMapper;
import org.bn.sensation.core.assistant.service.mapper.UpdateAssistantRequestMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantServiceImpl implements AssistantService {

    private final AssistantRepository assistantRepository;
    private final AssistantDtoMapper assistantDtoMapper;
    private final CreateAssistantRequestMapper createAssistantRequestMapper;
    private final UpdateAssistantRequestMapper updateAssistantRequestMapper;

    @Override
    public Page<AssistantDto> findAll(Pageable pageable) {
        return assistantRepository.findAll(pageable).map(assistantDtoMapper::toDto);
    }

    @Override
    public AssistantDto create(CreateAssistantRequest request) {
        AssistantEntity saved = assistantRepository.save(createAssistantRequestMapper.toEntity(request));
        return assistantDtoMapper.toDto(saved);
    }

    @Override
    public AssistantDto update(Long id, UpdateAssistantRequest request) {
        AssistantEntity assistant = assistantRepository.getByIdOrThrow(id);
        updateAssistantRequestMapper.updateParticipantFromRequest(request, assistant);
        AssistantEntity saved = assistantRepository.save(assistant);
        return assistantDtoMapper.toDto(saved);
    }

    @Override
    public void deleteById(Long id) {
        if (!assistantRepository.existsById(id)) {
            throw new EntityNotFoundException("Ассистент не найден с id: " + id);
        }
        assistantRepository.deleteById(id);
    }

    @Override
    public BaseRepository<AssistantEntity> getRepository() {
        return assistantRepository;
    }

    @Override
    public BaseDtoMapper<AssistantEntity, AssistantDto> getMapper() {
        return assistantDtoMapper;
    }
}
