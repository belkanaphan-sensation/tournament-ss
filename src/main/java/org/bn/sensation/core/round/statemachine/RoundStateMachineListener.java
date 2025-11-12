package org.bn.sensation.core.round.statemachine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RoundStateMachineListener implements StateMachineListener<RoundState, RoundEvent> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ThreadLocal для хранения ID раунда в текущем потоке
    private static final ThreadLocal<Long> ROUND_ID_CONTEXT = new ThreadLocal<>();

    // Map для хранения ID раунда по ID State Machine
    private static final Map<String, Long> STATE_MACHINE_TO_ROUND_ID = new ConcurrentHashMap<>();

    @Override
    public void stateChanged(State<RoundState, RoundEvent> from, State<RoundState, RoundEvent> to) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (from != null && to != null) {
            log.info("🔄 [ROUND_STATE_CHANGE] {} | {} → {} | Time: {}",
                getRoundId(), from.getId(), to.getId(), timestamp);
        } else if (to != null) {
            log.info("🆕 [ROUND_STATE_INIT] {} | Initial state: {} | Time: {}",
                getRoundId(), to.getId(), timestamp);
        }
    }

    @Override
    public void stateEntered(State<RoundState, RoundEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📥 [ROUND_STATE_ENTER] {} | Entered state: {} | Time: {}",
            getRoundId(), state.getId(), timestamp);
    }

    @Override
    public void stateExited(State<RoundState, RoundEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📤 [ROUND_STATE_EXIT] {} | Exited state: {} | Time: {}",
            getRoundId(), state.getId(), timestamp);
    }

    @Override
    public void eventNotAccepted(Message<RoundEvent> event) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.warn("❌ [ROUND_EVENT_REJECTED] {} | Event: {} | Time: {}",
            getRoundId(), event.getPayload(), timestamp);
    }

    @Override
    public void transition(Transition<RoundState, RoundEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (transition.getSource() != null && transition.getTarget() != null) {
            log.info("🔄 [ROUND_TRANSITION] {} | {} → {} | Event: {} | Time: {}",
                getRoundId(),
                transition.getSource().getId(),
                transition.getTarget().getId(),
                transition.getTrigger().getEvent(),
                timestamp);
        }
    }

    @Override
    public void transitionStarted(Transition<RoundState, RoundEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🚀 [ROUND_TRANSITION_START] {} | {} → {} | Event: {} | Time: {}",
            getRoundId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void transitionEnded(Transition<RoundState, RoundEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("✅ [ROUND_TRANSITION_END] {} | {} → {} | Event: {} | Time: {}",
            getRoundId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void stateMachineStarted(StateMachine<RoundState, RoundEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🟢 [ROUND_SM_START] {} | State Machine started | Time: {}",
            getRoundId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineStopped(StateMachine<RoundState, RoundEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🔴 [ROUND_SM_STOP] {} | State Machine stopped | Time: {}",
            getRoundId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineError(StateMachine<RoundState, RoundEvent> stateMachine, Exception exception) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.error("💥 [ROUND_SM_ERROR] {} | Error: {} | Time: {}",
            getRoundId(stateMachine), exception.getMessage(), timestamp, exception);
    }

    @Override
    public void extendedStateChanged(Object key, Object value) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🔧 [ROUND_EXTENDED_STATE] {} | Key: {} | Value: {} | Time: {}",
            getRoundId(), key, value, timestamp);
    }

    @Override
    public void stateContext(StateContext<RoundState, RoundEvent> stateContext) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        // Логируем контекст только для важных событий
        if (stateContext.getEvent() != null) {
            log.debug("📋 [ROUND_CONTEXT] {} | Event: {} | Headers: {} | Time: {}",
                getRoundId(),
                stateContext.getEvent(),
                stateContext.getMessageHeaders(),
                timestamp);
        }
    }

    /**
     * Устанавливает ID раунда для текущего потока
     */
    public static void setRoundId(Long roundId) {
        ROUND_ID_CONTEXT.set(roundId);
    }

    /**
     * Очищает ID раунда для текущего потока
     */
    public static void clearRoundId() {
        ROUND_ID_CONTEXT.remove();
    }

    /**
     * Регистрирует связь между State Machine и Round ID
     */
    public static void registerStateMachine(String stateMachineId, Long roundId) {
        STATE_MACHINE_TO_ROUND_ID.put(stateMachineId, roundId);
    }

    /**
     * Удаляет связь между State Machine и Round ID
     */
    public static void unregisterStateMachine(String stateMachineId) {
        if (stateMachineId != null) {
            STATE_MACHINE_TO_ROUND_ID.remove(stateMachineId);
        }
    }

    private String getRoundId() {
        // Сначала пытаемся получить из ThreadLocal
        Long roundId = ROUND_ID_CONTEXT.get();
        if (roundId != null) {
            return "ROUND_" + roundId;
        }

        // Если не найден, возвращаем общий идентификатор
        return "ROUND_" + Thread.currentThread().threadId();
    }

    private String getRoundId(StateMachine<RoundState, RoundEvent> stateMachine) {
        // Пытаемся получить ID раунда по ID State Machine
        String stateMachineId = stateMachine.getId();
        if (stateMachineId != null) {
            Long roundId = STATE_MACHINE_TO_ROUND_ID.get(stateMachineId);
            if (roundId != null) {
                return "ROUND_" + roundId;
            }
        }

        // Fallback к ThreadLocal
        return getRoundId();
    }
}
