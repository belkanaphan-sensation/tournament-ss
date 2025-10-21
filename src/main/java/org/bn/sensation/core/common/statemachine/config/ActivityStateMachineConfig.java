package org.bn.sensation.core.common.statemachine.config;

import java.util.EnumSet;

import org.bn.sensation.core.common.statemachine.action.ActivityAction;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
import org.bn.sensation.core.common.statemachine.guard.ActivityGuard;
import org.bn.sensation.core.common.statemachine.listener.ActivityStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.ActivityState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "activityStateMachine")
@RequiredArgsConstructor
public class ActivityStateMachineConfig extends EnumStateMachineConfigurerAdapter<ActivityState, ActivityEvent> {

    private final ActivityGuard activityGuard;
    private final ActivityAction activityAction;
    private final ActivityStateMachineListener activityStateMachineListener;

    @Override
    public void configure(StateMachineConfigurationConfigurer<ActivityState, ActivityEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(false)
                .listener(activityStateMachineListener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<ActivityState, ActivityEvent> states) throws Exception {
        states
                .withStates()
                .initial(ActivityState.DRAFT)
                .states(EnumSet.allOf(ActivityState.class));
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
                // PLANNED -> DRAFT
                .withExternal()
                .source(ActivityState.PLANNED)
                .target(ActivityState.DRAFT)
                .event(ActivityEvent.DRAFT)
                .guard(activityGuard)
                .action(activityAction)
                .and()
                // PLANNED -> REGISTRATION_CLOSED
                .withExternal()
                .source(ActivityState.PLANNED)
                .target(ActivityState.REGISTRATION_CLOSED)
                .event(ActivityEvent.CLOSE_REGISTRATION)
                .guard(activityGuard)
                .action(activityAction)
                .and()
                // REGISTRATION_CLOSED -> IN_PROGRESS
                .withExternal()
                .source(ActivityState.REGISTRATION_CLOSED)
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
