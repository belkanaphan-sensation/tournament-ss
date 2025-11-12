package org.bn.sensation.config.statemachine;

import java.util.EnumSet;

import org.bn.sensation.core.milestone.statemachine.MilestoneAction;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.milestone.statemachine.MilestoneStateMachineListener;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory(name = "milestoneStateMachine")
@RequiredArgsConstructor
public class MilestoneStateMachineConfig extends EnumStateMachineConfigurerAdapter<MilestoneState, MilestoneEvent> {

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
                // Настроили все правила. Участники могут быть добавлены или нет,
                // если это этап, зависящий от предыдущего
                // тут должно происходит создание раундов в статусе DRAFT
                // с автоматическим первичным распределением участников
                // DRAFT -> PLANNED
                .withExternal()
                .source(MilestoneState.DRAFT)
                .target(MilestoneState.PLANNED)
                .event(MilestoneEvent.PLAN)
                .action(milestoneAction)
                .and()
                // Если нужно поменять правила
                // PLANNED -> DRAFT
                .withExternal()
                .source(MilestoneState.PLANNED)
                .target(MilestoneState.DRAFT)
                .event(MilestoneEvent.DRAFT)
                .action(milestoneAction)
                .and()
                // Настроены все правила.
                // Участники добавлены в этап, но не распределены по раундам.
                // Когда произошло автоматическое распределение по раундам,
                // и админ проверил и добавил/удалил кого надо
                // PLANNED -> PENDING
                .withExternal()
                .source(MilestoneState.PLANNED)
                .target(MilestoneState.PENDING)
                .event(MilestoneEvent.PREPARE_ROUNDS)
                .action(milestoneAction)
                .and()
                // Админ проверил всех участников раунда, согласовал их и стартует этап.
                // Тут уже предполагается, что все раунды сформированы правильно,
                // но их редактирование возможно на случай, если что-то пошло не так,
                // например, кто-то выбыл с травмой и нужно добавить другого
                // PENDING -> IN_PROGRESS
                .withExternal()
                .source(MilestoneState.PENDING)
                .target(MilestoneState.IN_PROGRESS)
                .event(MilestoneEvent.START)
                .action(milestoneAction)
                .and()
                // Все раунды завершены и в статусе READY.
                // Можно посмотреть на предварительный результат этапа
                // и принять решение нужен ли дополнительный раунд или нет
                // Все раунды в состоянии READY (или COMPLETED?)
                // IN_PROGRESS -> SUMMARIZING
                .withExternal()
                .source(MilestoneState.IN_PROGRESS)
                .target(MilestoneState.SUMMARIZING)
                .event(MilestoneEvent.SUM_UP)
                .action(milestoneAction)
                .and()
                // Если дополнительный раунд понадобился, то его создали и этап продолжается
                // SUMMARIZING -> IN_PROGRESS
                .withExternal()
                .source(MilestoneState.SUMMARIZING)
                .target(MilestoneState.IN_PROGRESS)
                .event(MilestoneEvent.START)
                .action(milestoneAction)
                .and()
                // Все раунды, включая дополнительные, завершены, этап можно завершать,
                // получать его конечный результат и переходить к следующему
                // SUMMARIZING -> COMPLETED
                .withExternal()
                .source(MilestoneState.SUMMARIZING)
                .target(MilestoneState.COMPLETED)
                .event(MilestoneEvent.COMPLETE)
                .action(milestoneAction)
                .and()
                // SKIP MILESTONE
                .withExternal()
                .source(MilestoneState.DRAFT)
                .target(MilestoneState.SKIPPED)
                .event(MilestoneEvent.SKIP)
                .action(milestoneAction)
                .and()
                .withExternal()
                .source(MilestoneState.PLANNED)
                .target(MilestoneState.SKIPPED)
                .event(MilestoneEvent.SKIP)
                .action(milestoneAction)
                .and()
                .withExternal()
                .source(MilestoneState.PENDING)
                .target(MilestoneState.SKIPPED)
                .event(MilestoneEvent.SKIP)
                .action(milestoneAction)
                .and()
                .withExternal()
                .source(MilestoneState.IN_PROGRESS)
                .target(MilestoneState.SKIPPED)
                .event(MilestoneEvent.SKIP)
                .action(milestoneAction)
                .and()
                .withExternal()
                .source(MilestoneState.SUMMARIZING)
                .target(MilestoneState.SKIPPED)
                .event(MilestoneEvent.SKIP)
                .action(milestoneAction)
                .and()
                .withExternal()
                .source(MilestoneState.COMPLETED)
                .target(MilestoneState.SKIPPED)
                .event(MilestoneEvent.SKIP)
                .action(milestoneAction)
                .and()
                .withExternal()
                .source(MilestoneState.SKIPPED)
                .target(MilestoneState.DRAFT)
                .event(MilestoneEvent.DRAFT)
                .action(milestoneAction)
                .and()
                // ********
                // Следующие переходы не уверена пока, что нужны:
                // PENDING -> PLANNED
                .withExternal()
                .source(MilestoneState.PENDING)
                .target(MilestoneState.PLANNED)
                .event(MilestoneEvent.PLAN)
                .action(milestoneAction)
                .and()
                // IN_PROGRESS -> PENDING
                .withExternal()
                .source(MilestoneState.IN_PROGRESS)
                .target(MilestoneState.PENDING)
                .event(MilestoneEvent.PREPARE_ROUNDS)
                .action(milestoneAction);
    }
}
