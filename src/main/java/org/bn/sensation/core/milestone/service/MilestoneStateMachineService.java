package org.bn.sensation.core.milestone.service;

import org.bn.sensation.core.common.statemachine.service.StateMachineService;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;

public interface MilestoneStateMachineService extends StateMachineService<MilestoneEntity, MilestoneEvent> {
}
