package org.bn.sensation.core.activityresult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityresult.repository.ActivityResultRepository;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.dto.CreateActivityResultRequest;
import org.bn.sensation.core.activityresult.service.mapper.ActivityResultDtoMapper;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
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
    private final ParticipantRepository participantRepository;

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
        Map<Long, ParticipantEntity> participantMap = participantRepository.findAllByIdWithActivity(requests.stream().map(CreateActivityResultRequest::getParticipantId).toList())
                .stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        List<ActivityResultEntity> results = requests.stream()
                .map(req -> {
                    ParticipantEntity participant = Optional.ofNullable(participantMap.get(req.getParticipantId()))
                            .orElseThrow(() -> new IllegalArgumentException("Участник с ID %s не найден".formatted(req.getParticipantId())));
                    Preconditions.checkArgument(participant.getActivity().getId().longValue() == activity.getId().longValue(),
                            "Участник %s не принадлежит активности %s".formatted(participant.getId(), activity.getId()));
                    ActivityResultEntity result = ActivityResultEntity.builder()
                            .activity(activity)
                            .participant(participant)
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
