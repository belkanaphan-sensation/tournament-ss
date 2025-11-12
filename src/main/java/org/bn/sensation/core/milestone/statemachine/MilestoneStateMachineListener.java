package org.bn.sensation.core.milestone.statemachine;

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
public class MilestoneStateMachineListener implements StateMachineListener<MilestoneState, MilestoneEvent> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // ThreadLocal для хранения ID этапа в текущем потоке
    private static final ThreadLocal<Long> MILESTONE_ID_CONTEXT = new ThreadLocal<>();

    // Map для хранения ID этапа по ID State Machine
    private static final Map<String, Long> STATE_MACHINE_TO_MILESTONE_ID = new ConcurrentHashMap<>();

    @Override
    public void stateChanged(State<MilestoneState, MilestoneEvent> from, State<MilestoneState, MilestoneEvent> to) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (from != null && to != null) {
            log.info("🔄 [MILESTONE_STATE_CHANGE] {} | {} → {} | Time: {}",
                getMilestoneId(), from.getId(), to.getId(), timestamp);
        } else if (to != null) {
            log.info("🆕 [MILESTONE_STATE_INIT] {} | Initial state: {} | Time: {}",
                getMilestoneId(), to.getId(), timestamp);
        }
    }

    @Override
    public void stateEntered(State<MilestoneState, MilestoneEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📥 [MILESTONE_STATE_ENTER] {} | Entered state: {} | Time: {}",
            getMilestoneId(), state.getId(), timestamp);
    }

    @Override
    public void stateExited(State<MilestoneState, MilestoneEvent> state) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("📤 [MILESTONE_STATE_EXIT] {} | Exited state: {} | Time: {}",
            getMilestoneId(), state.getId(), timestamp);
    }

    @Override
    public void eventNotAccepted(Message<MilestoneEvent> event) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.warn("❌ [MILESTONE_EVENT_REJECTED] {} | Event: {} | Time: {}",
            getMilestoneId(), event.getPayload(), timestamp);
    }

    @Override
    public void transition(Transition<MilestoneState, MilestoneEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        if (transition.getSource() != null && transition.getTarget() != null) {
            log.info("🔄 [MILESTONE_TRANSITION] {} | {} → {} | Event: {} | Time: {}",
                getMilestoneId(),
                transition.getSource().getId(),
                transition.getTarget().getId(),
                transition.getTrigger().getEvent(),
                timestamp);
        }
    }

    @Override
    public void transitionStarted(Transition<MilestoneState, MilestoneEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🚀 [MILESTONE_TRANSITION_START] {} | {} → {} | Event: {} | Time: {}",
            getMilestoneId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void transitionEnded(Transition<MilestoneState, MilestoneEvent> transition) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("✅ [MILESTONE_TRANSITION_END] {} | {} → {} | Event: {} | Time: {}",
            getMilestoneId(),
            transition.getSource().getId(),
            transition.getTarget().getId(),
            transition.getTrigger().getEvent(),
            timestamp);
    }

    @Override
    public void stateMachineStarted(StateMachine<MilestoneState, MilestoneEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🟢 [MILESTONE_SM_START] {} | State Machine started | Time: {}",
            getMilestoneId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineStopped(StateMachine<MilestoneState, MilestoneEvent> stateMachine) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.info("🔴 [MILESTONE_SM_STOP] {} | State Machine stopped | Time: {}",
            getMilestoneId(stateMachine), timestamp);
    }

    @Override
    public void stateMachineError(StateMachine<MilestoneState, MilestoneEvent> stateMachine, Exception exception) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.error("💥 [MILESTONE_SM_ERROR] {} | Error: {} | Time: {}",
            getMilestoneId(stateMachine), exception.getMessage(), timestamp, exception);
    }

    @Override
    public void extendedStateChanged(Object key, Object value) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        log.debug("🔧 [MILESTONE_EXTENDED_STATE] {} | Key: {} | Value: {} | Time: {}",
            getMilestoneId(), key, value, timestamp);
    }

    @Override
    public void stateContext(StateContext<MilestoneState, MilestoneEvent> stateContext) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        // Логируем контекст только для важных событий
        if (stateContext.getEvent() != null) {
            log.debug("📋 [MILESTONE_CONTEXT] {} | Event: {} | Headers: {} | Time: {}",
                getMilestoneId(),
                stateContext.getEvent(),
                stateContext.getMessageHeaders(),
                timestamp);
        }
    }

    /**
     * Устанавливает ID этапа для текущего потока
     */
    public static void setMilestoneId(Long milestoneId) {
        MILESTONE_ID_CONTEXT.set(milestoneId);
    }

    /**
     * Очищает ID этапа для текущего потока
     */
    public static void clearMilestoneId() {
        MILESTONE_ID_CONTEXT.remove();
    }

    /**
     * Регистрирует связь между State Machine и Milestone ID
     */
    public static void registerStateMachine(String stateMachineId, Long milestoneId) {
        STATE_MACHINE_TO_MILESTONE_ID.put(stateMachineId, milestoneId);
    }

    /**
     * Удаляет связь между State Machine и Milestone ID
     */
    public static void unregisterStateMachine(String stateMachineId) {
        if (stateMachineId != null) {
            STATE_MACHINE_TO_MILESTONE_ID.remove(stateMachineId);
        }
    }

    private String getMilestoneId() {
        // Сначала пытаемся получить из ThreadLocal
        Long milestoneId = MILESTONE_ID_CONTEXT.get();
        if (milestoneId != null) {
            return "MILESTONE_" + milestoneId;
        }

        // Если не найден, возвращаем общий идентификатор
        return "MILESTONE_" + Thread.currentThread().threadId();
    }

    private String getMilestoneId(StateMachine<MilestoneState, MilestoneEvent> stateMachine) {
        // Пытаемся получить ID этапа по ID State Machine
        String stateMachineId = stateMachine.getId();
        if (stateMachineId != null) {
            Long milestoneId = STATE_MACHINE_TO_MILESTONE_ID.get(stateMachineId);
            if (milestoneId != null) {
                return "MILESTONE_" + milestoneId;
            }
        }

        // Fallback к ThreadLocal
        return getMilestoneId();
    }
}
