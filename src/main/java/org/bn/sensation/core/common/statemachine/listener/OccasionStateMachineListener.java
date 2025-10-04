package org.bn.sensation.core.common.statemachine.listener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bn.sensation.core.common.statemachine.event.OccasionEvent;
import org.bn.sensation.core.common.statemachine.state.OccasionState;
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
public class OccasionStateMachineListener implements StateMachineListener<OccasionState, OccasionEvent> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ThreadLocal для хранения ID мероприятия в текущем потоке
    private static final ThreadLocal<Long> OCCASION_ID_CONTEXT = new ThreadLocal<>();

    // Map для хранения ID мероприятия по ID State Machine
    private static final Map<String, Long> STATE_MACHINE_TO_OCCASION_ID = new ConcurrentHashMap<>();

    @Override
    public void stateChanged(State<OccasionState, OccasionEvent> from, State<OccasionState, OccasionEvent> to) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (from != null && to != null) {
            log.info("🔄 [OCCASION_STATE_CHANGE] {} | {} → {} | Time: {}",
                getOccasionId(), from.getId(), to.getId(), timestamp);
        } else if (to != null) {
            log.info("🆕 [OCCASION_STATE_INIT] {} | Initial state: {} | Time: {}",
                getOccasionId(), to.getId(), timestamp);
        }
    }

    @Override
    public void stateEntered(State<OccasionState, OccasionEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📥 [OCCASION_STATE_ENTER] {} | Entered state: {} | Time: {}",
            getOccasionId(), state.getId(), timestamp);
    }

    @Override
    public void stateExited(State<OccasionState, OccasionEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📤 [OCCASION_STATE_EXIT] {} | Exited state: {} | Time: {}",
            getOccasionId(), state.getId(), timestamp);
    }

    @Override
    public void eventNotAccepted(Message<OccasionEvent> event) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.warn("❌ [OCCASION_EVENT_REJECTED] {} | Event: {} | Time: {}",
            getOccasionId(), event.getPayload(), timestamp);
    }

    @Override
    public void transition(Transition<OccasionState, OccasionEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (transition.getSource() != null && transition.getTarget() != null) {
            log.info("🔄 [OCCASION_TRANSITION] {} | {} → {} | Event: {} | Time: {}",
                getOccasionId(),
                transition.getSource().getId(),
                transition.getTarget().getId(),
                transition.getTrigger().getEvent(),
                timestamp);
        }
    }

    @Override
    public void transitionStarted(Transition<OccasionState, OccasionEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🚀 [OCCASION_TRANSITION_START] {} | {} → {} | Event: {} | Time: {}",
            getOccasionId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void transitionEnded(Transition<OccasionState, OccasionEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("✅ [OCCASION_TRANSITION_END] {} | {} → {} | Event: {} | Time: {}",
            getOccasionId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void stateMachineStarted(StateMachine<OccasionState, OccasionEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🟢 [OCCASION_SM_START] {} | State Machine started | Time: {}",
            getOccasionId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineStopped(StateMachine<OccasionState, OccasionEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🔴 [OCCASION_SM_STOP] {} | State Machine stopped | Time: {}",
            getOccasionId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineError(StateMachine<OccasionState, OccasionEvent> stateMachine, Exception exception) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.error("💥 [OCCASION_SM_ERROR] {} | Error: {} | Time: {}",
            getOccasionId(stateMachine), exception.getMessage(), timestamp, exception);
    }

    @Override
    public void extendedStateChanged(Object key, Object value) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🔧 [OCCASION_EXTENDED_STATE] {} | Key: {} | Value: {} | Time: {}",
            getOccasionId(), key, value, timestamp);
    }

    @Override
    public void stateContext(StateContext<OccasionState, OccasionEvent> stateContext) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        // Логируем контекст только для важных событий
        if (stateContext.getEvent() != null) {
            log.debug("📋 [OCCASION_CONTEXT] {} | Event: {} | Headers: {} | Time: {}",
                getOccasionId(),
                stateContext.getEvent(),
                stateContext.getMessageHeaders(),
                timestamp);
        }
    }

    /**
     * Устанавливает ID мероприятия для текущего потока
     */
    public static void setOccasionId(Long occasionId) {
        OCCASION_ID_CONTEXT.set(occasionId);
    }

    /**
     * Очищает ID мероприятия для текущего потока
     */
    public static void clearOccasionId() {
        OCCASION_ID_CONTEXT.remove();
    }

    /**
     * Регистрирует связь между State Machine и Occasion ID
     */
    public static void registerStateMachine(String stateMachineId, Long occasionId) {
        STATE_MACHINE_TO_OCCASION_ID.put(stateMachineId, occasionId);
    }

    /**
     * Удаляет связь между State Machine и Occasion ID
     */
    public static void unregisterStateMachine(String stateMachineId) {
        if (stateMachineId != null) {
            STATE_MACHINE_TO_OCCASION_ID.remove(stateMachineId);
        }
    }

    private String getOccasionId() {
        // Сначала пытаемся получить из ThreadLocal
        Long occasionId = OCCASION_ID_CONTEXT.get();
        if (occasionId != null) {
            return "OCCASION_" + occasionId;
        }

        // Если не найден, возвращаем общий идентификатор
        return "OCCASION_" + Thread.currentThread().threadId();
    }

    private String getOccasionId(StateMachine<OccasionState, OccasionEvent> stateMachine) {
        // Пытаемся получить ID мероприятия по ID State Machine
        String stateMachineId = stateMachine.getId();
        if (stateMachineId != null) {
            Long occasionId = STATE_MACHINE_TO_OCCASION_ID.get(stateMachineId);
            if (occasionId != null) {
                return "OCCASION_" + occasionId;
            }
        }

        // Fallback к ThreadLocal
        return getOccasionId();
    }
}
