package org.bn.sensation.core.occasion.service;

import org.bn.sensation.core.common.statemachine.service.StateMachineService;
import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.occasion.entity.OccasionEntity;

public interface OccasionStateMachineService extends StateMachineService<OccasionEntity, OccasionEvent> {
}
