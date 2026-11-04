package org.team7525.onlineTrajectoryService.pathfinding;

import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Translation2d;

/**
 * One time-stamped desired state in a custom holonomic trajectory.
 *
 * <p>fieldVelocityMetersPerSecond is field-relative. angularVelocityRadPerSecond is positive CCW.
 */
public record TrajectorySample(
        double timeSeconds,
        Pose2d pose,
        Translation2d fieldVelocityMetersPerSecond,
        double angularVelocityRadPerSecond) {}
