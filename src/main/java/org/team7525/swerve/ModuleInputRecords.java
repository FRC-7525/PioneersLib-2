package org.team7525.swerve;

import org.wpilib.math.geometry.Rotation2d;

public final class ModuleInputRecords {
    public record ModuleData(DriveData driveData, TurnData turnData) {
        public ModuleData() {
            this(new DriveData(), new TurnData());
        }
    }

    public record DriveData(
            boolean connected,
            double positionRad,
            double velocityRadPerSec,
            double appliedVolts,
            double supplyCurrentAmps,
            double torqueCurrentAmps,
            double temperatureFahrenheit) {
        public DriveData() {
            this(false, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public record TurnData(
            boolean connected,
            boolean encoderConnected,
            Rotation2d absolutePosition,
            Rotation2d position,
            double turnVelocityRadPerSec,
            double turnAppliedVolts,
            double turnSupplyCurrentAmps,
            double turnTorqueCurrentAmps,
            double temperatureFahrenheit) {
        public TurnData() {
            this(false, false, Rotation2d.ZERO, Rotation2d.ZERO, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
