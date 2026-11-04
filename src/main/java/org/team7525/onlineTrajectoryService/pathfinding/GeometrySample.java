package org.team7525.onlineTrajectoryService.pathfinding;

import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;

public class GeometrySample {
    final Translation2d position;
    final Rotation2d tangent;
    final double curvatureRadPerMeter;
    final double distanceMeters;

    double maxVelocityMetersPerSecond;
    double velocityMetersPerSecond;
    double timeSeconds;

    /*
     * Initialized later by sampleBezierGeometry's caller.
     */
    double maxAccelerationMetersPerSecondSq = 0.0; // TODO: Check where this is used

    GeometrySample(
            Translation2d position,
            Rotation2d tangent,
            double curvatureRadPerMeter,
            double distanceMeters) {
        this.position = position;
        this.tangent = tangent;
        this.curvatureRadPerMeter = curvatureRadPerMeter;
        this.distanceMeters = distanceMeters;
    }
}
