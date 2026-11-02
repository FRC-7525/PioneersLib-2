package org.team7525.subsystem;

import static org.wpilib.units.Units.*;

import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.LinearAcceleration;
import org.wpilib.units.measure.LinearVelocity;

/**
 * A container for the state of a subsystem, prevents invalid states with eroneous vaues. If your
 * state is not included in one of the factory methods you need to split up your subsystem or tell
 * mech to build a different robot
 */
public record SubsystemState(
        String stateString,
        Distance position,
        Rotation2d angularPosition,
        LinearVelocity velocity,
        AngularVelocity angularVelocity,
        AngularVelocity secondaryVelocity,
        LinearAcceleration acceleration) {
    private static final Distance ZERO_POSITION = Meters.of(0);
    private static final LinearVelocity ZERO_VELOCITY = MetersPerSecond.of(0);
    private static final AngularVelocity ZERO_ANGULAR_VELOCITY = RotationsPerSecond.of(0);
    private static final LinearAcceleration ZERO_ACCELERATION = MetersPerSecondPerSecond.of(0);

    /** Primary constructor for all fields. Validates the state to prevent invalid combinations. */
    public SubsystemState {
        validateState();
    }

    /** Static factory method for defining a subsystem with pivot states */
    public static SubsystemState fromPivotStates(String stateString, Rotation2d angularPosition) {
        return new SubsystemState(
                stateString,
                ZERO_POSITION,
                angularPosition,
                ZERO_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION);
    }

    /** Static factory method for defining a subsystem with just elevator states */
    public static SubsystemState fromElevatorStates(
            String stateString, Distance position, LinearVelocity velocity) {
        return new SubsystemState(stateString, position, null, velocity, null, null, null);
    }

    /** Static factory method for defining a subsystem with just intake states */
    public static SubsystemState fromOpeningIntakeStates(
            String stateString, Rotation2d angularPosition, AngularVelocity angularVelocity) {
        return new SubsystemState(
                stateString, null, angularPosition, null, angularVelocity, null, null);
    }

    /** Static factory method for defining a subsystem with just intake states */
    public static SubsystemState fromIntakeStates(
            String stateString, AngularVelocity angularVelocity) {
        return new SubsystemState(stateString, null, null, null, angularVelocity, null, null);
    }

    /** Static factory method for defining a subsystem with just flywheel states */
    public static SubsystemState fromFlywheelStates(String stateString, AngularVelocity velocity) {
        return new SubsystemState(stateString, null, null, null, velocity, null, null);
    }

    /**
     * Static factory method for defining a subsystem with just flywheel states (uneven flywheels
     * for spin)
     */
    public static SubsystemState fromUnevenFlywheelStates(
            String stateString, AngularVelocity velocity, AngularVelocity secondaryVelocity) {
        return new SubsystemState(stateString, null, null, null, velocity, secondaryVelocity, null);
    }

    /** Static factory method for defining a subsystem with spinner and pivot states */
    public static SubsystemState fromPivotWithSpinnerStates(
            String stateString, Rotation2d angularPosition, AngularVelocity angularVelocity) {
        return new SubsystemState(
                stateString, null, angularPosition, null, angularVelocity, null, null);
    }

    /**
     * Static factory method for defining a subsystem with just turret states (mech pls build this)
     */
    public static SubsystemState fromTurretStates(
            String stateString, Rotation2d angularPosition, AngularVelocity angularVelocity) {
        return new SubsystemState(
                stateString, null, angularPosition, null, angularVelocity, null, null);
    }

    /** Validates state to ensure it's not like really dumb. */
    private void validateState() throws IllegalStateException {
        double pos = position != null ? position.in(Meters) : 0;
        double vel = velocity != null ? velocity.in(MetersPerSecond) : 0;
        double angVel = angularVelocity != null ? angularVelocity.in(RotationsPerSecond) : 0;
        double secVel = secondaryVelocity != null ? secondaryVelocity.in(RPM) / 60 : 0;
        double accel = acceleration != null ? acceleration.in(MetersPerSecondPerSecond) : 0;

        if (pos > 10) {
            throw new IllegalStateException(
                    "Position must be less than or equal to 10. There is likely a unit conversion error.");
        }

        if (vel > 40) {
            throw new IllegalStateException(
                    "Velocity must be less than 40m/sec. There is likely a unit conversion issue.");
        }

        if (angVel > 200 || secVel > 200) {
            throw new IllegalStateException(
                    "Improbable that a motor is going 200RPS - Either the gearing is too low or the code is bad.");
        }

        if (accel > 30) {
            throw new IllegalStateException("Acceleration cannot be greater than 30 m/s^2");
        }
    }
}
