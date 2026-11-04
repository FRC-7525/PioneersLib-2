package org.team7525.subsystem;

import java.util.Collections;
import java.util.Map;

public class State {
    private final String stateString;
    private final Runnable runnable;
    private final SubsystemState subsystemState;
    private final Map<String, Object> customFields;

    public String getStateString() {
        return stateString;
    }

    public State(
            String stateString,
            Runnable runnable,
            SubsystemState subsystemState,
            Map<String, Object> customFields) {
        this.stateString = stateString;
        this.runnable = runnable;
        this.subsystemState = subsystemState;
        this.customFields =
                customFields == null ? Collections.emptyMap() : Map.copyOf(customFields);
    }

    public State(String stateString, Runnable runnable, SubsystemState subsystemState) {
        this(stateString, runnable, subsystemState, null);
    }

    public State(String stateString, SubsystemState subsystemState) {
        this(stateString, null, subsystemState, null);
    }

    public State(String stateString, Runnable runnable, Map<String, Object> customFields) {
        this(stateString, runnable, null, customFields);
    }

    public State(String stateString, Map<String, Object> customFields) {
        this(stateString, null, null, customFields);
    }

    public State(String stateString, Runnable runnable) {
        this(stateString, runnable, null, null);
    }

    public State(String stateString) {
        this(stateString, (Runnable) null, null, null);
    }

    public Map<String, Object> getCustomFields() {
        return customFields;
    }

    /**
     * Runs the state's custom action, or the subsystem's normal state hook when none is supplied.
     */
    void run(ManagedSubsystem<?> subsystem) {
        if (runnable != null) {
            runnable.run();
        } else {
            subsystem.runState();
        }
    }

    public SubsystemState getSubsystemState() {
        return subsystemState;
    }
}
