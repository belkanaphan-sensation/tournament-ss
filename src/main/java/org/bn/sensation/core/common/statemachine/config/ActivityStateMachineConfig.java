package org.bn.sensation.core.common.statemachine.config;

import java.util.EnumSet;

import org.bn.sensation.core.common.statemachine.action.ActivityAction;
import org.bn.sensation.core.common.statemachine.event.ActivityEvent;
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
                .initial(ActivityState.PLANNED)
                .states(EnumSet.allOf(ActivityState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ActivityState, ActivityEvent> transitions) throws Exception {
        transitions
                // PLANNED -> REGISTRATION_CLOSED
                .withExternal()
                .source(ActivityState.PLANNED)
                .target(ActivityState.REGISTRATION_CLOSED)
                .event(ActivityEvent.CLOSE_REGISTRATION)
                .action(activityAction)
                .and()
                // REGISTRATION_CLOSED -> PLANNED
                .withExternal()
                .source(ActivityState.REGISTRATION_CLOSED)
                .target(ActivityState.PLANNED)
                .event(ActivityEvent.PLAN)
                .action(activityAction)
                .and()
                // REGISTRATION_CLOSED -> IN_PROGRESS
                .withExternal()
                .source(ActivityState.REGISTRATION_CLOSED)
                .target(ActivityState.IN_PROGRESS)
                .event(ActivityEvent.START)
                .action(activityAction)
                .and()
                // IN_PROGRESS -> SUMMARIZING
                .withExternal()
                .source(ActivityState.IN_PROGRESS)
                .target(ActivityState.SUMMARIZING)
                .event(ActivityEvent.SUM_UP)
                .action(activityAction)
                .and()
                // SUMMARIZING -> COMPLETED
                .withExternal()
                .source(ActivityState.SUMMARIZING)
                .target(ActivityState.COMPLETED)
                .event(ActivityEvent.COMPLETE)
                .action(activityAction);
    }
}
