package org.bn.sensation.core.activity.statemachine;

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
public class ActivityStateMachineListener implements StateMachineListener<ActivityState, ActivityEvent> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ThreadLocal для хранения ID активности в текущем потоке
    private static final ThreadLocal<Long> ACTIVITY_ID_CONTEXT = new ThreadLocal<>();

    // Map для хранения ID активности по ID State Machine
    private static final Map<String, Long> STATE_MACHINE_TO_ACTIVITY_ID = new ConcurrentHashMap<>();

    @Override
    public void stateChanged(State<ActivityState, ActivityEvent> from, State<ActivityState, ActivityEvent> to) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (from != null && to != null) {
            log.info("🔄 [ACTIVITY_STATE_CHANGE] {} | {} → {} | Time: {}",
                getActivityId(), from.getId(), to.getId(), timestamp);
        } else if (to != null) {
            log.info("🆕 [ACTIVITY_STATE_INIT] {} | Initial state: {} | Time: {}",
                getActivityId(), to.getId(), timestamp);
        }
    }

    @Override
    public void stateEntered(State<ActivityState, ActivityEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📥 [ACTIVITY_STATE_ENTER] {} | Entered state: {} | Time: {}",
            getActivityId(), state.getId(), timestamp);
    }

    @Override
    public void stateExited(State<ActivityState, ActivityEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📤 [ACTIVITY_STATE_EXIT] {} | Exited state: {} | Time: {}",
            getActivityId(), state.getId(), timestamp);
    }

    @Override
    public void eventNotAccepted(Message<ActivityEvent> event) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.warn("❌ [ACTIVITY_EVENT_REJECTED] {} | Event: {} | Time: {}",
            getActivityId(), event.getPayload(), timestamp);
    }

    @Override
    public void transition(Transition<ActivityState, ActivityEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (transition.getSource() != null && transition.getTarget() != null) {
            log.info("🔄 [ACTIVITY_TRANSITION] {} | {} → {} | Event: {} | Time: {}",
                getActivityId(),
                transition.getSource().getId(),
                transition.getTarget().getId(),
                transition.getTrigger().getEvent(),
                timestamp);
        }
    }

    @Override
    public void transitionStarted(Transition<ActivityState, ActivityEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🚀 [ACTIVITY_TRANSITION_START] {} | {} → {} | Event: {} | Time: {}",
            getActivityId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void transitionEnded(Transition<ActivityState, ActivityEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("✅ [ACTIVITY_TRANSITION_END] {} | {} → {} | Event: {} | Time: {}",
            getActivityId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void stateMachineStarted(StateMachine<ActivityState, ActivityEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🟢 [ACTIVITY_SM_START] {} | State Machine started | Time: {}",
            getActivityId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineStopped(StateMachine<ActivityState, ActivityEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🔴 [ACTIVITY_SM_STOP] {} | State Machine stopped | Time: {}",
            getActivityId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineError(StateMachine<ActivityState, ActivityEvent> stateMachine, Exception exception) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.error("💥 [ACTIVITY_SM_ERROR] {} | Error: {} | Time: {}",
            getActivityId(stateMachine), exception.getMessage(), timestamp, exception);
    }

    @Override
    public void extendedStateChanged(Object key, Object value) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🔧 [ACTIVITY_EXTENDED_STATE] {} | Key: {} | Value: {} | Time: {}",
            getActivityId(), key, value, timestamp);
    }

    @Override
    public void stateContext(StateContext<ActivityState, ActivityEvent> stateContext) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        // Логируем контекст только для важных событий
        if (stateContext.getEvent() != null) {
            log.debug("📋 [ACTIVITY_CONTEXT] {} | Event: {} | Headers: {} | Time: {}",
                getActivityId(),
                stateContext.getEvent(),
                stateContext.getMessageHeaders(),
                timestamp);
        }
    }

    /**
     * Устанавливает ID активности для текущего потока
     */
    public static void setActivityId(Long activityId) {
        ACTIVITY_ID_CONTEXT.set(activityId);
    }

    /**
     * Очищает ID активности для текущего потока
     */
    public static void clearActivityId() {
        ACTIVITY_ID_CONTEXT.remove();
    }

    /**
     * Регистрирует связь между State Machine и Activity ID
     */
    public static void registerStateMachine(String stateMachineId, Long activityId) {
        STATE_MACHINE_TO_ACTIVITY_ID.put(stateMachineId, activityId);
    }

    /**
     * Удаляет связь между State Machine и Activity ID
     */
    public static void unregisterStateMachine(String stateMachineId) {
        if (stateMachineId != null) {
            STATE_MACHINE_TO_ACTIVITY_ID.remove(stateMachineId);
        }
    }

    private String getActivityId() {
        // Сначала пытаемся получить из ThreadLocal
        Long activityId = ACTIVITY_ID_CONTEXT.get();
        if (activityId != null) {
            return "ACTIVITY_" + activityId;
        }

        // Если не найден, возвращаем общий идентификатор
        return "ACTIVITY_" + Thread.currentThread().threadId();
    }

    private String getActivityId(StateMachine<ActivityState, ActivityEvent> stateMachine) {
        // Пытаемся получить ID активности по ID State Machine
        String stateMachineId = stateMachine.getId();
        if (stateMachineId != null) {
            Long activityId = STATE_MACHINE_TO_ACTIVITY_ID.get(stateMachineId);
            if (activityId != null) {
                return "ACTIVITY_" + activityId;
            }
        }

        // Fallback к ThreadLocal
        return getActivityId();
    }
}
