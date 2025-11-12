package org.bn.sensation.core.common.statemachine.config;

import java.util.EnumSet;

import org.bn.sensation.core.common.statemachine.action.OccasionAction;
import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.listener.OccasionStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "occasionStateMachine")
@RequiredArgsConstructor
public class OccasionStateMachineConfig extends EnumStateMachineConfigurerAdapter<OccasionState, OccasionEvent> {

    private final OccasionAction occasionAction;
    private final OccasionStateMachineListener occasionStateMachineListener;

    @Override
    public void configure(StateMachineConfigurationConfigurer<OccasionState, OccasionEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(false)
                .listener(occasionStateMachineListener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OccasionState, OccasionEvent> states) throws Exception {
        states
                .withStates()
                .initial(OccasionState.PLANNED)
                .states(EnumSet.allOf(OccasionState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OccasionState, OccasionEvent> transitions) throws Exception {
        transitions
                // PLANNED -> IN_PROGRESS
                .withExternal()
                .source(OccasionState.PLANNED)
                .target(OccasionState.IN_PROGRESS)
                .event(OccasionEvent.START)
                .action(occasionAction)
                .and()
                // IN_PROGRESS -> PLANNED
                .withExternal()
                .source(OccasionState.IN_PROGRESS)
                .target(OccasionState.PLANNED)
                .event(OccasionEvent.PLAN)
                .action(occasionAction)
                .and()
                // IN_PROGRESS -> COMPLETED
                .withExternal()
                .source(OccasionState.IN_PROGRESS)
                .target(OccasionState.COMPLETED)
                .event(OccasionEvent.COMPLETE)
                .action(occasionAction);
    }
}
