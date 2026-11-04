package org.team7525.onlineTrajectoryService.zones;

import org.team7525.onlineTrajectoryService.obstacles.Obstacle;
import org.wpilib.math.geometry.Translation2d;

/** Adapts an existing Geometry.Obstacle shape into a non-blocking Zone. */
public class ShapeZone implements Zone {
    private final String name;
    private final Obstacle shape;

    public ShapeZone(String name, Obstacle shape) {
        this.name = name;
        this.shape = shape;
    }

    @Override
    public boolean contains(Translation2d position) {
        return shape.contains(position);
    }

    @Override
    public String getName() {
        return name;
    }
}
