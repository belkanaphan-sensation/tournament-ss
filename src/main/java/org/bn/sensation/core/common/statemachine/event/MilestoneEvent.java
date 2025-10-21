package org.bn.sensation.core.common.statemachine.event;

import org.bn.sensation.core.common.statemachine.BaseEvent;

public enum MilestoneEvent implements BaseEvent {
    DRAFT, PLAN, PREPARE_ROUNDS, START, SUM_UP, COMPLETE
}
