package org.bn.sensation.core.activityresult;

import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityresult.repository.ActivityResultRepository;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.mapper.ActivityResultDtoMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityResultServiceImpl implements ActivityResultService{

    private final ActivityResultRepository activityResultRepository;
    private final ActivityResultDtoMapper activityResultDtoMapper;

    @Override
    public BaseRepository<ActivityResultEntity> getRepository() {
        return activityResultRepository;
    }

    @Override
    public BaseDtoMapper<ActivityResultEntity, ActivityResultDto> getMapper() {
        return activityResultDtoMapper;
    }
}
