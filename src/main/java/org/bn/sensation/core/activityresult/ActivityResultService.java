package org.bn.sensation.core.activityresult;

import java.util.List;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activityresult.entity.ActivityResultEntity;
import org.bn.sensation.core.activityresult.service.dto.ActivityResultDto;
import org.bn.sensation.core.activityresult.service.dto.CreateActivityResultRequest;
import org.bn.sensation.core.common.service.BaseService;

import jakarta.validation.constraints.NotNull;

public interface ActivityResultService extends BaseService<ActivityResultEntity, ActivityResultDto> {

    List<ActivityResultDto> createActivityResults(ActivityEntity activity, List<CreateActivityResultRequest> requests);

    List<ActivityResultDto> getByActivityId(@NotNull Long activityId);
}
