package org.bn.sensation.core.activityresult;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityresult.repository.ActivityResultRepository;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.mapper.ActivityResultDtoMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.milestone.entity.AssessmentMode;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityResultServiceImpl implements ActivityResultService{

    private final ActivityResultRepository activityResultRepository;
    private final ActivityResultDtoMapper activityResultDtoMapper;
    private final MilestoneRepository milestoneRepository;

    @Override
    public BaseRepository<ActivityResultEntity> getRepository() {
        return activityResultRepository;
    }

    @Override
    public BaseDtoMapper<ActivityResultEntity, ActivityResultDto> getMapper() {
        return activityResultDtoMapper;
    }

    @Override
    @Transactional
    public List<ActivityResultDto> calculateActivityResults(Long activityId) {
        log.info("Начинаем подсчет результатов для мероприятия: {}", activityId);
        MilestoneEntity milestone = milestoneRepository.getByActivityIdAndMilestoneOrderOrThrow(activityId, 0);


        return List.of();
    }



    private Comparator<BigDecimal> getScoreComparator(MilestoneEntity milestone) {
        return milestone.getMilestoneRule().getAssessmentMode() != AssessmentMode.PLACE
                ? Comparator.reverseOrder()
                : Comparator.naturalOrder();
    }
}
