package org.team7525.onlineTrajectoryService.obstacles;

import java.util.List;
import org.team7525.geometry.Bounds;
import org.wpilib.math.geometry.Translation2d;

public class TriangularObstacle implements Obstacle {
    private final PolygonObstacle polygon;

    public TriangularObstacle(Translation2d first, Translation2d second, Translation2d third) {
        polygon = new PolygonObstacle(List.of(first, second, third));
    }

    public TriangularObstacle(
            Translation2d first,
            Translation2d second,
            Translation2d third,
            double inflationRadius) {
        polygon = new PolygonObstacle(List.of(first, second, third), inflationRadius);
    }

    @Override
    public boolean contains(Translation2d point) {
        return polygon.contains(point);
    }

    @Override
    public Bounds getBounds() {
        return polygon.getBounds();
    }
}
