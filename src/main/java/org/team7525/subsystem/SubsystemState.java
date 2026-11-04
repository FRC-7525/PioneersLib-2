package org.team7525.subsystem;

import static org.wpilib.units.Units.*;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.LinearAcceleration;
import org.wpilib.units.measure.LinearVelocity;
import org.wpilib.units.measure.Voltage;

/**
 * A container for the state of a subsystem, prevents invalid states with eroneous vaues. If your
 * state is not included in one of the factory methods you need to split up your subsystem or tell
 * mech to build a different robot
 */
public record SubsystemState(
        Distance linearPosition,
        Angle angularPosition,
        LinearVelocity linearVelocity,
        AngularVelocity angularVelocity,
        AngularVelocity secondaryAngularVelocity,
        LinearAcceleration linearAcceleration,
        Voltage primaryVoltage,
        Pose2d pose2d) {
    private static final Distance ZERO_POSITION = Meters.of(0);
    private static final Angle ZERO_ANGULAR_POSITION = Radians.of(0);
    private static final LinearVelocity ZERO_VELOCITY = MetersPerSecond.of(0);
    private static final AngularVelocity ZERO_ANGULAR_VELOCITY = RotationsPerSecond.of(0);
    private static final LinearAcceleration ZERO_ACCELERATION = MetersPerSecondPerSecond.of(0);
    private static final Voltage ZERO_VOLTAGE = Volts.of(0);
    private static final Pose2d ZERO_POSE2D = new Pose2d();

    /** Primary constructor for all fields. Validates the state to prevent invalid combinations. */
    public SubsystemState {
        validateState();
    }

    /** Static factory method for defining a subsystem with pivot states */
    public static SubsystemState fromPivotStates(Angle angularPosition) {
        return new SubsystemState(
                ZERO_POSITION,
                angularPosition,
                ZERO_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    /** Static factory method for defining a subsystem with just elevator states */
    public static SubsystemState fromElevatorStates(Distance position, LinearVelocity velocity) {
        return new SubsystemState(
                position,
                ZERO_ANGULAR_POSITION,
                velocity,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    /** Static factory method for defining a subsystem with just intake states */
    public static SubsystemState fromPivotingIntakeStates(
            Angle angularPosition, AngularVelocity angularVelocity) {
        return new SubsystemState(
                ZERO_POSITION,
                angularPosition,
                ZERO_VELOCITY,
                angularVelocity,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    public static SubsystemState fromPivotingIntakeStates(Angle angularPosition, Voltage voltage) {
        return new SubsystemState(
                ZERO_POSITION,
                angularPosition,
                ZERO_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                voltage,
                ZERO_POSE2D);
    }

    /** Static factory method for defining a subsystem with just intake states */
    public static SubsystemState fromIntakeStates(AngularVelocity angularVelocity) {
        return new SubsystemState(
                ZERO_POSITION,
                ZERO_ANGULAR_POSITION,
                ZERO_VELOCITY,
                angularVelocity,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    public static SubsystemState fromIntakeStates(Voltage voltage) {
        return new SubsystemState(
                ZERO_POSITION,
                ZERO_ANGULAR_POSITION,
                ZERO_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                voltage,
                ZERO_POSE2D);
    }

    /** Static factory method for defining a subsystem with just flywheel states */
    public static SubsystemState fromFlywheelStates(AngularVelocity velocity) {
        return new SubsystemState(
                ZERO_POSITION,
                ZERO_ANGULAR_POSITION,
                ZERO_VELOCITY,
                velocity,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    /**
     * Static factory method for defining a subsystem with just flywheel states (uneven flywheels
     * for spin)
     */
    public static SubsystemState fromUnevenFlywheelStates(
            AngularVelocity velocity, AngularVelocity secondaryVelocity) {
        return new SubsystemState(
                ZERO_POSITION,
                ZERO_ANGULAR_POSITION,
                ZERO_VELOCITY,
                velocity,
                secondaryVelocity,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    /** Static factory method for defining a subsystem with spinner and pivot states */
    public static SubsystemState fromPivotWithSpinnerStates(
            Angle angularPosition, AngularVelocity angularVelocity) {
        return new SubsystemState(
                ZERO_POSITION,
                angularPosition,
                ZERO_VELOCITY,
                angularVelocity,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    /**
     * Static factory method for defining a subsystem with just turret states (mech pls build this)
     */
    public static SubsystemState fromTurretStates(
            Angle angularPosition, AngularVelocity angularVelocity) {
        return new SubsystemState(
                ZERO_POSITION,
                angularPosition,
                ZERO_VELOCITY,
                angularVelocity,
                ZERO_ANGULAR_VELOCITY,
                ZERO_ACCELERATION,
                ZERO_VOLTAGE,
                ZERO_POSE2D);
    }

    /** Validates state to ensure it's not like really dumb. */
    private void validateState() throws IllegalStateException {
        double pos = linearPosition != null ? linearPosition.in(Meters) : 0;
        double vel = linearVelocity != null ? linearVelocity.in(MetersPerSecond) : 0;
        double angVel = angularVelocity != null ? angularVelocity.in(RotationsPerSecond) : 0;
        double secVel =
                secondaryAngularVelocity != null ? secondaryAngularVelocity.in(RPM) / 60 : 0;
        double accel =
                linearAcceleration != null ? linearAcceleration.in(MetersPerSecondPerSecond) : 0;
        double voltageVal = primaryVoltage != null ? primaryVoltage.in(Volts) : 0;
        double poseX = pose2d != null ? pose2d.getX() : 0;
        double poseY = pose2d != null ? pose2d.getY() : 0;

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

        if (voltageVal > 12) {
            throw new IllegalStateException("Primary voltage cannot exceed 12V");
        }

        if (poseX > 10) {
            throw new IllegalStateException("Pose X coordinate cannot exceed 10 meters");
        }

        if (poseY > 10) {
            throw new IllegalStateException("Pose Y coordinate cannot exceed 10 meters");
        }
    }
}
