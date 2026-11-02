// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package org.team7525.seasonSpecific;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.wpilib.fields.Field;
import org.wpilib.fields.Fields;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.geometry.Translation3d;
import org.wpilib.math.util.Units;

/**
 * Contains information for locations of field elements and other useful reference points.
 *
 * <p>All constants are defined relative to the field coordinate system and from the perspective of
 * the blue alliance station.
 *
 * <p>This file currently uses the WPILib 2026 REBUILT AprilTag layout. Replace the layout constant
 * when WPILib releases the official 2027 field layout.
 */
public class FieldConstants {
    public static final FieldType fieldType = FieldType.WELDED;

    // AprilTag-related constants
    public static final AprilTagLayoutType defaultAprilTagType = AprilTagLayoutType.OFFICIAL;

    public static final Field aprilTagLayout = defaultAprilTagType.getLayout();

    public static final int aprilTagCount = aprilTagLayout.getTags().size();

    public static final double aprilTagWidth = Units.inchesToMeters(6.5);

    // Field dimensions
    public static final double fieldLength = aprilTagLayout.getFieldLength();

    public static final double fieldWidth = aprilTagLayout.getFieldWidth();

    public static final Translation2d fieldCenter =
            new Translation2d(fieldLength / 2.0, fieldWidth / 2.0);

    // Fuel dimensions
    public static final double fuelDiameter = Units.inchesToMeters(5.91);

    public static final double trenchBarWidth = Units.inchesToMeters(2.95);

    /**
     * Officially defined and relevant vertical lines found on the field, defined by X-axis offset.
     */
    public static class LinesVertical {
        public static final double center = fieldLength / 2.0;

        public static final double starting = aprilTagLayout.getTagPose(26).orElseThrow().getX();

        public static final double allianceZone = starting;

        public static final double hubCenter =
                aprilTagLayout.getTagPose(26).orElseThrow().getX() + Hub.width / 2.0;

        public static final double neutralZoneNear = center - Units.inchesToMeters(120.0);

        public static final double neutralZoneFar = center + Units.inchesToMeters(120.0);

        public static final double oppHubCenter =
                aprilTagLayout.getTagPose(4).orElseThrow().getX() + Hub.width / 2.0;

        public static final double oppAllianceZone =
                aprilTagLayout.getTagPose(10).orElseThrow().getX();
    }

    /**
     * Officially defined and relevant horizontal lines found on the field, defined by Y-axis
     * offset.
     *
     * <p>Field element start and end positions are defined from left to right from the perspective
     * of the alliance station.
     */
    public static class LinesHorizontal {
        public static final double center = fieldWidth / 2.0;

        // Right of hub
        public static final double rightBumpStart = Hub.nearRightCorner.getY();

        public static final double rightBumpEnd = rightBumpStart - RightBump.width;

        public static final double rightBumpMiddle = (rightBumpStart + rightBumpEnd) / 2.0;

        public static final double rightTrenchOpenStart = rightBumpEnd - Units.inchesToMeters(12.0);

        public static final double rightTrenchOpenEnd = 0.0;

        public static final double rightTrenchMiddle =
                (rightTrenchOpenStart + rightTrenchOpenEnd) / 2.0;

        // Left of hub
        public static final double leftBumpEnd = Hub.nearLeftCorner.getY();

        public static final double leftBumpStart = leftBumpEnd + LeftBump.width;

        public static final double leftBumpMiddle = (leftBumpStart + leftBumpEnd) / 2.0;

        public static final double leftTrenchOpenEnd = leftBumpStart + Units.inchesToMeters(12.0);

        public static final double leftTrenchOpenStart = fieldWidth;

        public static final double leftTrenchMiddle =
                (leftTrenchOpenEnd + leftTrenchOpenStart) / 2.0;
    }

    /** Hub-related constants. */
    public static class Hub {
        // Dimensions
        public static final double width = Units.inchesToMeters(47.0);

        public static final double height = Units.inchesToMeters(72.0);

        public static final double innerWidth = Units.inchesToMeters(41.7);

        public static final double innerHeight = Units.inchesToMeters(56.5);

        // Relevant reference points on alliance side
        public static final Translation3d topCenterPoint =
                new Translation3d(
                        aprilTagLayout.getTagPose(26).orElseThrow().getX() + width / 2.0,
                        fieldWidth / 2.0,
                        height);

        public static final Translation3d innerCenterPoint =
                new Translation3d(
                        aprilTagLayout.getTagPose(26).orElseThrow().getX() + width / 2.0,
                        fieldWidth / 2.0,
                        innerHeight);

        public static final Translation2d nearLeftCorner =
                new Translation2d(
                        topCenterPoint.getX() - width / 2.0, fieldWidth / 2.0 + width / 2.0);

        public static final Translation2d nearRightCorner =
                new Translation2d(
                        topCenterPoint.getX() - width / 2.0, fieldWidth / 2.0 - width / 2.0);

        public static final Translation2d farLeftCorner =
                new Translation2d(
                        topCenterPoint.getX() + width / 2.0, fieldWidth / 2.0 + width / 2.0);

        public static final Translation2d farRightCorner =
                new Translation2d(
                        topCenterPoint.getX() + width / 2.0, fieldWidth / 2.0 - width / 2.0);

        // Hub faces
        public static final Pose2d nearFace =
                aprilTagLayout.getTagPose(26).orElseThrow().toPose2d();

        public static final Pose2d farFace = aprilTagLayout.getTagPose(20).orElseThrow().toPose2d();

        public static final Pose2d rightFace =
                aprilTagLayout.getTagPose(18).orElseThrow().toPose2d();

        public static final Pose2d leftFace =
                aprilTagLayout.getTagPose(21).orElseThrow().toPose2d();
    }

    /** Left bump-related constants. */
    public static class LeftBump {
        // Dimensions
        public static final double width = Units.inchesToMeters(73.0);

        public static final double height = Units.inchesToMeters(6.513);

        public static final double depth = Units.inchesToMeters(44.4);

        // Relevant reference points on alliance side
        public static final Translation2d nearLeftCorner =
                Hub.nearLeftCorner.plus(new Translation2d(0.0, width));

        public static final Translation2d nearRightCorner = Hub.nearLeftCorner;

        public static final Translation2d farLeftCorner =
                Hub.farLeftCorner.plus(new Translation2d(0.0, width));

        public static final Translation2d farRightCorner = Hub.farLeftCorner;
    }

    /** Right bump-related constants. */
    public static class RightBump {
        // Dimensions
        public static final double width = Units.inchesToMeters(73.0);

        public static final double height = Units.inchesToMeters(6.513);

        public static final double depth = Units.inchesToMeters(44.4);

        // Relevant reference points on alliance side
        public static final Translation2d nearLeftCorner = Hub.nearRightCorner;

        public static final Translation2d nearRightCorner =
                Hub.nearRightCorner.minus(new Translation2d(0.0, width));

        public static final Translation2d farLeftCorner = Hub.farRightCorner;

        public static final Translation2d farRightCorner =
                Hub.farRightCorner.minus(new Translation2d(0.0, width));
    }

    /** Left trench-related constants. */
    public static class LeftTrench {
        // Dimensions
        public static final double width = Units.inchesToMeters(65.65);

        public static final double depth = Units.inchesToMeters(47.0);

        public static final double height = Units.inchesToMeters(40.25);

        public static final double openingWidth = Units.inchesToMeters(50.34);

        public static final double openingHeight = Units.inchesToMeters(22.25);

        // Relevant reference points on alliance side
        public static final Translation3d openingTopLeft =
                new Translation3d(LinesVertical.hubCenter, fieldWidth, openingHeight);

        public static final Translation3d openingTopRight =
                new Translation3d(
                        LinesVertical.hubCenter, fieldWidth - openingWidth, openingHeight);

        public static final Translation2d center =
                openingTopLeft
                        .toTranslation2d()
                        .interpolate(openingTopRight.toTranslation2d(), 0.5);
    }

    /** Right trench-related constants. */
    public static class RightTrench {
        // Dimensions
        public static final double width = Units.inchesToMeters(65.65);

        public static final double depth = Units.inchesToMeters(47.0);

        public static final double height = Units.inchesToMeters(40.25);

        public static final double openingWidth = Units.inchesToMeters(50.34);

        public static final double openingHeight = Units.inchesToMeters(22.25);

        // Relevant reference points on alliance side
        public static final Translation3d openingTopLeft =
                new Translation3d(LinesVertical.hubCenter, openingWidth, openingHeight);

        public static final Translation3d openingTopRight =
                new Translation3d(LinesVertical.hubCenter, 0.0, openingHeight);

        public static final Translation2d center =
                openingTopLeft
                        .toTranslation2d()
                        .interpolate(openingTopRight.toTranslation2d(), 0.5);
    }

    /** Tower-related constants. */
    public static class Tower {
        // Dimensions
        public static final double width = Units.inchesToMeters(49.25);

        public static final double depth = Units.inchesToMeters(45.0);

        public static final double height = Units.inchesToMeters(78.25);

        public static final double innerOpeningWidth = Units.inchesToMeters(32.250);

        public static final double frontFaceX = Units.inchesToMeters(43.51);

        public static final double uprightHeight = Units.inchesToMeters(72.1);

        // Rung heights from the floor
        public static final double lowRungHeight = Units.inchesToMeters(27.0);

        public static final double midRungHeight = Units.inchesToMeters(45.0);

        public static final double highRungHeight = Units.inchesToMeters(63.0);

        private static final double tagY = aprilTagLayout.getTagPose(31).orElseThrow().getY();

        // Relevant reference points on alliance side
        public static final Translation2d centerPoint = new Translation2d(frontFaceX, tagY);

        public static final Translation2d leftUpright =
                new Translation2d(
                        frontFaceX, tagY + innerOpeningWidth / 2.0 + Units.inchesToMeters(0.75));

        public static final Translation2d rightUpright =
                new Translation2d(
                        frontFaceX, tagY - innerOpeningWidth / 2.0 - Units.inchesToMeters(0.75));
    }

    /** Depot-related constants. */
    public static class Depot {
        // Dimensions
        public static final double width = Units.inchesToMeters(42.0);

        public static final double depth = Units.inchesToMeters(27.0);

        public static final double height = Units.inchesToMeters(1.125);

        public static final double distanceFromCenterY = Units.inchesToMeters(75.93);

        // Relevant reference points on alliance side
        public static final Translation3d depotCenter =
                new Translation3d(depth, fieldWidth / 2.0 + distanceFromCenterY, height);

        public static final Translation3d leftCorner =
                new Translation3d(
                        depth, fieldWidth / 2.0 + distanceFromCenterY + width / 2.0, height);

        public static final Translation3d rightCorner =
                new Translation3d(
                        depth, fieldWidth / 2.0 + distanceFromCenterY - width / 2.0, height);
    }

    /** Outpost-related constants. */
    public static class Outpost {
        // Dimensions
        public static final double width = Units.inchesToMeters(31.8);

        public static final double openingDistanceFromFloor = Units.inchesToMeters(28.1);

        public static final double height = Units.inchesToMeters(7.0);

        // Relevant reference points on alliance side
        public static final Translation2d centerPoint =
                new Translation2d(0.0, aprilTagLayout.getTagPose(29).orElseThrow().getY());
    }

    /** Fuel-pool-related constants. */
    public static class FuelPool {
        // Dimensions
        public static final double width = Units.inchesToMeters(181.9);

        public static final double depth = Units.inchesToMeters(71.9);

        // Relevant reference points on alliance side
        public static final Translation2d nearLeftCorner =
                new Translation2d(fieldLength / 2.0 - depth / 2.0, fieldWidth / 2.0 + width / 2.0);

        public static final Translation2d nearRightCorner =
                new Translation2d(fieldLength / 2.0 - depth / 2.0, fieldWidth / 2.0 - width / 2.0);

        public static final Translation2d leftCenter =
                new Translation2d(fieldLength / 2.0, fieldWidth / 2.0 + width / 2.0);

        public static final Translation2d rightCenter =
                new Translation2d(fieldLength / 2.0, fieldWidth / 2.0 - width / 2.0);
    }

    @RequiredArgsConstructor
    public enum FieldType {
        HQ("hq"),
        ANDYMARK("andymark"),
        WELDED("welded");

        @Getter private final String jsonFolder;
    }

    /**
     * AprilTag layouts supplied by WPILib.
     *
     * <p>WPILib currently provides the 2026 REBUILT layout. Replace this with the official 2027
     * WPILib layout once it is released.
     */
    public enum AprilTagLayoutType {
        OFFICIAL(Fields.FRC_2026_REBUILT_ANDY_MARK);

        private final Fields field;
        private volatile Field layout;

        AprilTagLayoutType(Fields field) {
            this.field = field;
        }

        public Field getLayout() {
            if (layout == null) {
                synchronized (this) {
                    if (layout == null) {
                        try {
                            layout = field.loadField();
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Unable to load AprilTag field layout: " + field, e);
                        }
                    }
                }
            }

            return layout;
        }
    }
}
