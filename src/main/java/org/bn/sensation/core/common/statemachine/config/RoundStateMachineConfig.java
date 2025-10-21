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
                // Автоматически созданный раунд просмотрел админ,
                // добавил или удалил участников.
                // Раунд готов к началу, этап может переходить в IN_PROGRESS
                // DRAFT -> PLANNED
                .withExternal()
                .source(RoundState.DRAFT)
                .target(RoundState.PLANNED)
                .event(RoundEvent.PLAN)
                .action(roundAction)
                .and()
                // Нужно отредактировать участников. Этап может быть в DRAFT, PLANNED, PENDING или IN_PROGRESS
                // PLANNED -> DRAFT
                .withExternal()
                .source(RoundState.PLANNED)
                .target(RoundState.DRAFT)
                .event(RoundEvent.DRAFT)
                .action(roundAction)
                .and()
                // Можно стартовать раунд. Этап тоже в IN_PROGRESS
                // По ивенту START все статусы судей для данного раунда переходят в NOT_READY
                // Все результаты судей для данного раунда стираются, если они были,
                // т.к. раунд считается только что начавшимся
                // PLANNED -> IN_PROGRESS
                .withExternal()
                .source(RoundState.PLANNED)
                .target(RoundState.IN_PROGRESS)
                .event(RoundEvent.START)
                .action(roundAction)
                .and()
                // Если внезапно случилась необходимость кого то добавить/удалить
                // Этап остается в IN_PROGRESS
                // IN_PROGRESS -> DRAFT
                .withExternal()
                .source(RoundState.IN_PROGRESS)
                .target(RoundState.DRAFT)
                .event(RoundEvent.DRAFT)
                .action(roundAction)
                .and()
                // Когда все судьи проставили свои результаты и все статусы судей по раунду READY
                // Админ видит и понимает, что его можно завершать и переходить к следующему
                // IN_PROGRESS -> READY
                .withExternal()
                .source(RoundState.IN_PROGRESS)
                .target(RoundState.READY)
                .event(RoundEvent.MARK_READY)
                .action(roundAction)
                .and()
                // Когда все судьи уже завершили оценку, но кто-то откатил свой статус на NOT_READY
                // READY -> IN_PROGRESS
                .withExternal()
                .source(RoundState.READY)
                .target(RoundState.IN_PROGRESS)
                .event(RoundEvent.START)
                .action(roundAction)
                .and()
                // Когда результаты у всех судей готовы и окончательные
                // READY -> COMPLETED
                .withExternal()
                .source(RoundState.READY)
                .target(RoundState.COMPLETED)
                .event(RoundEvent.COMPLETE)
                .action(roundAction)
                .and()
                // ********
                // Следующие переходы не уверена пока, что нужны:
                // COMPLETED -> IN_PROGRESS
                .withExternal()
                .source(RoundState.COMPLETED)
                .target(RoundState.IN_PROGRESS)
                .event(RoundEvent.START)
                .action(roundAction)
                .and()
                // IN_PROGRESS -> PLANNED
                .withExternal()
                .source(RoundState.IN_PROGRESS)
                .target(RoundState.PLANNED)
                .event(RoundEvent.PLAN)
                .action(roundAction);
    }
}
