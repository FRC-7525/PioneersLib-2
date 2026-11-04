package org.team7525.onlineTrajectoryService.pathfinding;

import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;

/**
 * Information supplied to a custom HeadingFunction.
 *
 * @param position Current desired field position
 * @param pathTangent Direction of translational travel
 * @param distanceMeters Distance traveled along path
 * @param totalDistanceMeters Total path length
 * @param progress Path progress from 0.0 to 1.0
 * @param timeSeconds Planned trajectory time
 * @param totalTimeSeconds Planned trajectory duration
 */
public record HeadingContext(
        Translation2d position,
        Rotation2d pathTangent,
        double distanceMeters,
        double totalDistanceMeters,
        double progress,
        double timeSeconds,
        double totalTimeSeconds) {}
