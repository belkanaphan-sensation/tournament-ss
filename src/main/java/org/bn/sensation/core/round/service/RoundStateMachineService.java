package org.bn.sensation.core.round.service;

import org.bn.sensation.core.common.statemachine.service.StateMachineService;
import org.bn.sensation.core.round.statemachine.RoundEvent;
import org.bn.sensation.core.round.entity.RoundEntity;

public interface RoundStateMachineService extends StateMachineService<RoundEntity, RoundEvent> {
}
