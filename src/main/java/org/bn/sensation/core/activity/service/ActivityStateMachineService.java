package org.bn.sensation.core.activity.service;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.statemachine.service.StateMachineService;
import org.bn.sensation.core.activity.statemachine.ActivityEvent;

public interface ActivityStateMachineService extends StateMachineService<ActivityEntity, ActivityEvent> {
}
