package org.team7525.subsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StateMachineTest {
    private enum State implements SubsystemStates {
        IDLE,
        ACTIVE
    }

    private static final class TestManagedSubsystem extends ManagedSubsystem<State> {
        private int runnableCount;

        TestManagedSubsystem() {
            super("test", State.IDLE);
        }

        @Override
        protected void runState() {
            runnableCount++;
        }
    }

    private static final class TestManagerSubsystem extends RougeSubsystem<State> {
        TestManagerSubsystem() {
            super("test", State.IDLE);
        }

        @Override
        protected void runState() {}
    }

    @Test
    void managedSubsystemEvaluatesTriggersAndRunnableActions() {
        TestManagedSubsystem subsystem = new TestManagedSubsystem();

        subsystem.periodic();

        assertEquals(State.IDLE, subsystem.getState());
        assertEquals(1, subsystem.runnableCount);
    }

    @Test
    void transitionToStateWithoutTriggersDoesNotThrow() {
        TestManagerSubsystem subsystem = new TestManagerSubsystem();

        subsystem.setState(State.ACTIVE);

        assertEquals(State.ACTIVE, subsystem.getState());
    }
}
