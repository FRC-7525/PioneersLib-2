package org.team7525.onlineTrajectoryService.zones;

import org.wpilib.math.geometry.Translation2d;

/**
 * Any region of the field with contains() behavior.
 *
 * <p>Zones are distinct from obstacles: - Obstacles prevent planning/travel. - Zones apply
 * constraints, trigger actions, or provide semantic regions.
 */
public interface Zone {
    boolean contains(Translation2d position);

    String getName();
}
