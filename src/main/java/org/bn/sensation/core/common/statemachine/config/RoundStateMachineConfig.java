package org.bn.sensation.core.common.statemachine.config;

import org.bn.sensation.core.common.statemachine.action.RoundAction;
import org.bn.sensation.core.common.statemachine.event.RoundEvent;
import org.bn.sensation.core.common.statemachine.listener.RoundStateMachineListener;
import org.bn.sensation.core.common.statemachine.state.RoundState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "roundStateMachine")
@RequiredArgsConstructor
public class RoundStateMachineConfig extends EnumStateMachineConfigurerAdapter<RoundState, RoundEvent> {

    private final RoundAction roundAction;
    private final RoundStateMachineListener roundStateMachineListener;

    @Override
    public void configure(StateMachineConfigurationConfigurer<RoundState, RoundEvent> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(false)
                .listener(roundStateMachineListener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<RoundState, RoundEvent> states) throws Exception {
        states
                .withStates()
                .initial(RoundState.DRAFT)
                .states(java.util.EnumSet.allOf(RoundState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<RoundState, RoundEvent> transitions) throws Exception {
        transitions
                // DRAFT -> PLANNED
                .withExternal()
                .source(RoundState.DRAFT)
                .target(RoundState.PLANNED)
                .event(RoundEvent.PLAN)
                .action(roundAction)
                .and()
                // PLANNED -> DRAFT
                .withExternal()
                .source(RoundState.PLANNED)
                .target(RoundState.DRAFT)
                .event(RoundEvent.DRAFT)
                .action(roundAction)
                .and()
                // PLANNED -> IN_PROGRESS
                .withExternal()
                .source(RoundState.PLANNED)
                .target(RoundState.IN_PROGRESS)
                .event(RoundEvent.START)
                .action(roundAction)
                .and()
                // IN_PROGRESS -> PLANNED
                .withExternal()
                .source(RoundState.IN_PROGRESS)
                .target(RoundState.PLANNED)
                .event(RoundEvent.PLAN)
                .action(roundAction)
                .and()
                // IN_PROGRESS -> READY
                .withExternal()
                .source(RoundState.IN_PROGRESS)
                .target(RoundState.READY)
                .event(RoundEvent.CONFIRM)
                .action(roundAction)
                .and()
                // READY -> IN_PROGRESS
                .withExternal()
                .source(RoundState.READY)
                .target(RoundState.IN_PROGRESS)
                .event(RoundEvent.START)
                .action(roundAction)
                .and()
                // READY -> COMPLETED
                .withExternal()
                .source(RoundState.READY)
                .target(RoundState.COMPLETED)
                .event(RoundEvent.COMPLETE)
                .action(roundAction)
                .and()
                // COMPLETED -> IN_PROGRESS
                .withExternal()
                .source(RoundState.COMPLETED)
                .target(RoundState.IN_PROGRESS)
                .event(RoundEvent.START)
                .action(roundAction);
    }
}
