package org.bn.sensation.core.common.statemachine.config;

import org.bn.sensation.core.common.statemachine.action.OccasionAction;
import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.guard.OccasionGuard;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "occasionStateMachine")
@RequiredArgsConstructor
public class OccasionStateMachineConfig extends StateMachineConfigurerAdapter<OccasionState, OccasionEvent> {

    private final OccasionGuard occasionGuard;
    private final OccasionAction occasionAction;

    @Override
    public void configure(StateMachineStateConfigurer<OccasionState, OccasionEvent> states) throws Exception {
        states
            .withStates()
            .initial(OccasionState.DRAFT)
            .states(java.util.EnumSet.allOf(OccasionState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OccasionState, OccasionEvent> transitions) throws Exception {
        transitions
            // DRAFT -> PLANNED
            .withExternal()
                .source(OccasionState.DRAFT)
                .target(OccasionState.PLANNED)
                .event(OccasionEvent.PLAN)
                .guard(occasionGuard)
                .action(occasionAction)
                .and()
            // PLANNED -> IN_PROGRESS
            .withExternal()
                .source(OccasionState.PLANNED)
                .target(OccasionState.IN_PROGRESS)
                .event(OccasionEvent.START)
                .guard(occasionGuard)
                .action(occasionAction)
                .and()
            // IN_PROGRESS -> COMPLETED
            .withExternal()
                .source(OccasionState.IN_PROGRESS)
                .target(OccasionState.COMPLETED)
                .event(OccasionEvent.COMPLETE)
                .guard(occasionGuard)
                .action(occasionAction);
    }
}
