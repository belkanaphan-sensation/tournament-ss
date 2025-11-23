package org.bn.sensation.core.activityresult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityresult.repository.ActivityResultRepository;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.dto.CreateActivityResultRequest;
import org.bn.sensation.core.activityresult.service.mapper.ActivityResultDtoMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.repository.ContestantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityResultServiceImpl implements ActivityResultService {

    private final ActivityResultRepository activityResultRepository;
    private final ActivityResultDtoMapper activityResultDtoMapper;
    private final ContestantRepository contestantRepository;

    @Override
    public BaseRepository<ActivityResultEntity> getRepository() {
        return activityResultRepository;
    }

    @Override
    public BaseDtoMapper<ActivityResultEntity, ActivityResultDto> getMapper() {
        return activityResultDtoMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<ActivityResultDto> createActivityResults(ActivityEntity activity, List<CreateActivityResultRequest> requests) {
        Map<Long, ContestantEntity> contestants = contestantRepository.findAllById(requests.stream().map(CreateActivityResultRequest::getContestantId).toList())
                .stream()
                .collect(Collectors.toMap(ContestantEntity::getId, Function.identity()));
        List<ActivityResultEntity> results = requests.stream()
                .map(req -> {
                    ContestantEntity contestant = contestants.get(req.getContestantId());
                    Preconditions.checkArgument(contestant != null, "Конкурсант с id %s не найден".formatted(req.getContestantId()));
                    ActivityResultEntity result = ActivityResultEntity.builder()
                            .activity(activity)
                            .contestant(contestant)
                            .place(req.getPlace())
                            .build();
                    return result;
                }).toList();
        List<ActivityResultEntity> saved = activityResultRepository.saveAll(results);
        return saved.stream()
                .sorted(Comparator.comparing(ActivityResultEntity::getPlace))
                .map(activityResultDtoMapper::toDto)
                .toList();
    }
}
