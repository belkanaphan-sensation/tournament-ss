package org.bn.sensation.core.common.statemachine.config;

import org.bn.sensation.core.common.statemachine.action.ActivityAction;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.guard.ActivityGuard;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "activityStateMachine")
@RequiredArgsConstructor
public class ActivityStateMachineConfig extends StateMachineConfigurerAdapter<ActivityState, ActivityEvent> {

    private final ActivityGuard activityGuard;
    private final ActivityAction activityAction;

    @Override
    public void configure(StateMachineStateConfigurer<ActivityState, ActivityEvent> states) throws Exception {
        states
            .withStates()
            .initial(ActivityState.DRAFT)
            .states(java.util.EnumSet.allOf(ActivityState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ActivityState, ActivityEvent> transitions) throws Exception {
        transitions
            // DRAFT -> PLANNED
            .withExternal()
                .source(ActivityState.DRAFT)
                .target(ActivityState.PLANNED)
                .event(ActivityEvent.PLAN)
                .guard(activityGuard)
                .action(activityAction)
                .and()
            // PLANNED -> IN_PROGRESS
            .withExternal()
                .source(ActivityState.PLANNED)
                .target(ActivityState.IN_PROGRESS)
                .event(ActivityEvent.START)
                .guard(activityGuard)
                .action(activityAction)
                .and()
            // IN_PROGRESS -> COMPLETED
            .withExternal()
                .source(ActivityState.IN_PROGRESS)
                .target(ActivityState.COMPLETED)
                .event(ActivityEvent.COMPLETE)
                .guard(activityGuard)
                .action(activityAction);
    }
}
