package org.team7525.onlineTrajectoryService.pathfinding;

import org.wpilib.math.controller.PIDController;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.math.util.MathUtil;

public class HolonomicPoseController {
    private final PIDController xController;
    private final PIDController yController;
    private final PIDController rotationController;

    private final double maxLinearSpeedMetersPerSecond;
    private final double maxAngularSpeedRadiansPerSecond;

    public HolonomicPoseController(
            double translationKP,
            double translationKI,
            double translationKD,
            double rotationKP,
            double rotationKI,
            double rotationKD,
            double maxLinearSpeedMetersPerSecond,
            double maxAngularSpeedRadiansPerSecond) {
        xController = new PIDController(translationKP, translationKI, translationKD);

        yController = new PIDController(translationKP, translationKI, translationKD);

        rotationController = new PIDController(rotationKP, rotationKI, rotationKD);

        rotationController.enableContinuousInput(-Math.PI, Math.PI);

        this.maxLinearSpeedMetersPerSecond = maxLinearSpeedMetersPerSecond;
        this.maxAngularSpeedRadiansPerSecond = maxAngularSpeedRadiansPerSecond;
    }

    public void reset() {
        xController.reset();
        yController.reset();
        rotationController.reset();
    }

    /**
     * Calculate a robot-relative command to approach a stationary target pose.
     *
     * @param currentPose Current estimated field pose
     * @param targetPose Desired field pose
     * @return Robot-relative chassis-speed command
     */
    public ChassisVelocities calculate(
            Pose2d currentPose,
            Pose2d targetPose,
            Translation2d fieldVelocityFeedforward,
            double omegaFeedforwardRadPerSecond) {
        double fieldVx =
                fieldVelocityFeedforward.getX()
                        + xController.calculate(currentPose.getX(), targetPose.getX());

        double fieldVy =
                fieldVelocityFeedforward.getY()
                        + yController.calculate(currentPose.getY(), targetPose.getY());

        double omega =
                omegaFeedforwardRadPerSecond
                        + rotationController.calculate(
                                currentPose.getRotation().getRadians(),
                                targetPose.getRotation().getRadians());

        Translation2d translationCommand = new Translation2d(fieldVx, fieldVy);

        if (translationCommand.getNorm() > maxLinearSpeedMetersPerSecond) {
            translationCommand =
                    translationCommand.times(
                            maxLinearSpeedMetersPerSecond / translationCommand.getNorm());
        }

        omega =
                Math.clamp(
                        omega, -maxAngularSpeedRadiansPerSecond, maxAngularSpeedRadiansPerSecond);

        return new ChassisVelocities(translationCommand.getX(), translationCommand.getY(), omega)
                .toRobotRelative(currentPose.getRotation()); // TODO: CHeck this?
    }

    public boolean atTarget(
            Pose2d currentPose,
            Pose2d targetPose,
            double positionToleranceMeters,
            double headingToleranceRadians) {
        double positionError =
                currentPose.getTranslation().getDistance(targetPose.getTranslation());

        double headingError =
                Math.abs(
                        MathUtil.angleModulus(
                                targetPose.getRotation().getRadians()
                                        - currentPose.getRotation().getRadians()));

        return positionError <= positionToleranceMeters && headingError <= headingToleranceRadians;
    }
}
