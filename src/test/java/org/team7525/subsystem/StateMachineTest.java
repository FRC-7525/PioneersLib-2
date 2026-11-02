package org.team7525.subsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StateMachineTest {
    private enum State implements SubsystemStates {
        IDLE,
        ACTIVE
    }

    private static final class TestManagedSubsystem extends ManagedSubsystem<State> {
        private boolean condition;
        private int runnableCount;

        TestManagedSubsystem() {
            super("test", State.IDLE);
            addTrigger(State.IDLE, State.ACTIVE, () -> condition);
            addRunnableTrigger(() -> runnableCount++, () -> condition);
        }

        @Override
        protected void runState() {}
    }

    private static final class TestManagerSubsystem extends ManagerSubsystem<State> {
        TestManagerSubsystem() {
            super("test", State.IDLE);
        }

        @Override
        protected void runState() {}
    }

    @Test
    void managedSubsystemEvaluatesTriggersAndRunnableActions() {
        TestManagedSubsystem subsystem = new TestManagedSubsystem();
        subsystem.condition = true;

        subsystem.periodic();

        assertEquals(State.ACTIVE, subsystem.getState());
        assertEquals(1, subsystem.runnableCount);
    }

    @Test
    void transitionToStateWithoutTriggersDoesNotThrow() {
        TestManagerSubsystem subsystem = new TestManagerSubsystem();

        subsystem.setState(State.ACTIVE);

        assertEquals(State.ACTIVE, subsystem.getState());
    }
}
