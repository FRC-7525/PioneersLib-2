package org.team7525.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Translation2d;

class GeometryUtilTest {
    @Test
    void boundsContainsEdgesAndClampsOutsideValues() {
        Bounds bounds = new Bounds(1, 3, 2, 4);

        assertTrue(bounds.contains(new Translation2d(1, 4)));
        assertEquals(new Translation2d(1, 4), bounds.clamp(new Translation2d(-2, 8)));
    }

    @Test
    void poseInverseCancelsPose() {
        Pose2d pose = new Pose2d(2, 1, Rotation2d.fromDegrees(30));

        Pose2d identity = pose.transformBy(GeomUtil.toTransform2d(GeomUtil.inverse(pose)));

        assertEquals(0.0, identity.getX(), 1e-9);
        assertEquals(0.0, identity.getY(), 1e-9);
        assertEquals(0.0, identity.getRotation().getRadians(), 1e-9);
    }
}
