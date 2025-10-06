package org.bn.sensation.core.common.statemachine.config;

import java.util.EnumSet;

import org.bn.sensation.core.common.statemachine.action.MilestoneAction;
import org.bn.sensation.core.common.statemachine.event.MilestoneEvent;
import org.bn.sensation.core.common.statemachine.guard.MilestoneGuard;
import org.bn.sensation.core.common.statemachine.listener.MilestoneStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.MilestoneState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "milestoneStateMachine")
@RequiredArgsConstructor
public class MilestoneStateMachineConfig extends StateMachineConfigurerAdapter<MilestoneState, MilestoneEvent> {

    private final MilestoneGuard milestoneGuard;
    private final MilestoneAction milestoneAction;
    private final MilestoneStateMachineListener milestoneStateMachineListener;

    @Override
    public void configure(StateMachineConfigurationConfigurer<MilestoneState, MilestoneEvent> config) throws Exception {
        config
            .withConfiguration()
            .autoStartup(false)
            .listener(milestoneStateMachineListener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<MilestoneState, MilestoneEvent> states) throws Exception {
        states
            .withStates()
            .initial(MilestoneState.DRAFT)
            .states(EnumSet.allOf(MilestoneState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<MilestoneState, MilestoneEvent> transitions) throws Exception {
        transitions
            // DRAFT -> PLANNED
            .withExternal()
                .source(MilestoneState.DRAFT)
                .target(MilestoneState.PLANNED)
                .event(MilestoneEvent.PLAN)
                .guard(milestoneGuard)
                .action(milestoneAction)
                .and()
            // PLANNED -> IN_PROGRESS
            .withExternal()
                .source(MilestoneState.PLANNED)
                .target(MilestoneState.IN_PROGRESS)
                .event(MilestoneEvent.START)
                .guard(milestoneGuard)
                .action(milestoneAction)
                .and()
            // IN_PROGRESS -> COMPLETED
            .withExternal()
                .source(MilestoneState.IN_PROGRESS)
                .target(MilestoneState.COMPLETED)
                .event(MilestoneEvent.COMPLETE)
                .guard(milestoneGuard)
                .action(milestoneAction);
    }
}
