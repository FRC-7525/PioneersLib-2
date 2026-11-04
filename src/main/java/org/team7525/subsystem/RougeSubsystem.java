package org.team7525.subsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public abstract class RougeSubsystem<StateType extends Enum<StateType> & SubsystemStates>
        extends ManagedSubsystem<StateType> {

    private Map<StateType, ArrayList<Trigger<StateType>>> triggerMap = new HashMap<>();
    private List<RunnableTrigger> runnableTriggerList = new ArrayList<>();

    public RougeSubsystem(String subsystemName, StateType defaultState) {
        super(subsystemName, defaultState);
    }

    // State operation
    public void periodic() {
        super.periodic();

        checkTriggers();
        checkRunnableTriggers();
    }

    public void afterPeriodic() {
        // Custom logic to be executed after the periodic method
    }

    /**
     * Triggers for state transitions
     *
     * @param startType The {@link StateType} for starting
     * @param endType The {@link StateType} for ending
     * @param condition A {@link BooleanSupplier} that triggers the state transition
     */
    protected void addTrigger(StateType startType, StateType endType, BooleanSupplier condition) {
        triggerMap
                .computeIfAbsent(startType, k -> new ArrayList<>())
                .add(new Trigger<>(condition, endType));
    }

    protected void addRunnableTrigger(Runnable runnable, BooleanSupplier check) {
        runnableTriggerList.add(new RunnableTrigger(check, runnable));
    }

    private void checkTriggers() {
        List<Trigger<StateType>> triggers = triggerMap.get(state);
        if (triggers == null) return;

        for (var trigger : triggers) {
            if (trigger.isTriggered()) {
                setState(trigger.getResultState());
                return; // Exit after the first trigger to avoid multiple state changes in one cycle
            }
        }
    }

    private void checkRunnableTriggers() {
        for (var trigger : runnableTriggerList) {
            if (trigger.isTriggered()) {
                trigger.run();
            }
        }
    }
}
