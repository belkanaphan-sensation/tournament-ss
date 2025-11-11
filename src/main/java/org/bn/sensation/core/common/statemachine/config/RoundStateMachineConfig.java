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
                .initial(RoundState.OPENED)
                .states(java.util.EnumSet.allOf(RoundState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<RoundState, RoundEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(RoundState.OPENED)
                .target(RoundState.CLOSED)
                .event(RoundEvent.CLOSE)
                .action(roundAction);
    }
}
