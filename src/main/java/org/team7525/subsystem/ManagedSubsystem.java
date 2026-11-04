package org.team7525.subsystem;

import org.littletonrobotics.junction.Logger;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.hal.SimDevice.Direction;
import org.wpilib.system.Timer;

public abstract class ManagedSubsystem<StateType extends Enum<StateType> & SubsystemStates>
        implements Mechanism {
    protected StateType state = null;
    private Timer stateTimer = new Timer();
    private String subsystemName;

    public ManagedSubsystem(String subsystemName, StateType defaultState) {
        if (defaultState == null) {
            throw new RuntimeException("Default state cannot be null!");
        } else if (subsystemName == null || subsystemName.isEmpty()) {
            throw new RuntimeException("Subsystem name cannot be null or empty!");
        }
        this.subsystemName = subsystemName;
        this.state = defaultState;

        stateTimer.start();
    }

    // State operation
    public void periodic() {
        Logger.recordOutput(
                subsystemName + "/state", state.getStateString()); // TODO: Maybe problematic

        state.getState().run(this);
    }
    
    public void afterPeriodic() {
        // Custom logic to be executed after the periodic method
    }
    /** Fallback hook for states whose behavior depends on the subsystem instance. */
    protected void runState() {}

    /** Called AFTER the subsystem is set to a new state. Override to implement functionality */
    protected void stateInit() {}
    ;

    /** Called BEFORE the subsystem is set to a new state. Override to implement functionality */
    protected void stateExit() {}
    ;

    // Other utilities
    public StateType getState() {
        return state;
    }

    public void setState(StateType state) {
        if (this.state == state) return;

        stateTimer.reset();
        stateExit();

        this.state = state;
        stateInit();
    }

    public String getSubsystemName() {
        return subsystemName;
    }

    /**
     * Gets amount of time the state machine has been in the current state.
     *
     * @return time in seconds.
     */
    protected double getStateTime() {
        return stateTimer.get();
    }

    public Command sysIdDynamic(Direction direction) {
        return run(coroutine -> {
                    System.out.println("Please Override Me!");
                })
                .named("Override Warning - SysIdDynamic");
    }

    public Command sysIdQuasistatic(Direction direction) {
        return run(coroutine -> {
                    System.out.println("Please Override Me!");
                })
                .named("Override Warning - SysIdQuasistatic");
    }
}
