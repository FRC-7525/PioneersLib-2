package org.team7525.simpleSim;

import static org.wpilib.units.Units.KilogramSquareMeters;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.Rotations;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.sim.TalonFXSimState;
import com.revrobotics.sim.SparkMaxSim;
import org.team7525.util.GearRatio;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.SingleJointedArmSim;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.MomentOfInertia;
import org.wpilib.units.measure.Voltage;

/**
 * A simplified wrapper for simulating a single-jointed FRC arm. Manages physics, 3D odometry
 * updates, and unit-correct synchronization to REV SparkMax and CTRE TalonFX simulated motor
 * controllers.
 */
public class SimpleArm {
    private final SingleJointedArmSim armSim;
    private final GearRatio gearRatio;
    private final Axis axisOfRotation;
    private Pose3d pose3d;

    /**
     * Constructs a new SimpleArm physics simulation.
     *
     * @param gearbox The physical motor configuration (e.g. DCMotor.NEO(1)).
     * @param gearRatio The mechanical gear reduction from the motor to the arm pivot.
     * @param armLength The physical length of the arm.
     * @param armMOI The calculated moment of inertia of the arm.
     * @param armMinAngle The minimum physical angle limit.
     * @param armMaxAngle The maximum physical angle limit.
     * @param armInitialAngle The starting angle of the arm upon boot.
     * @param isGravityCompensationEnabled Whether to simulate gravity acting on the arm mass.
     * @param axisOfRotation The 3D axis the arm pivots around (for AdvantageScope/Field3d
     *     visualization).
     */
    public SimpleArm(
            DCMotor gearbox,
            GearRatio gearRatio,
            Distance armLength,
            MomentOfInertia armMOI,
            Angle armMinAngle,
            Angle armMaxAngle,
            Angle armInitialAngle,
            boolean isGravityCompensationEnabled,
            Axis axisOfRotation) {
        this.gearRatio = gearRatio;
        this.axisOfRotation = axisOfRotation;

        armSim =
                new SingleJointedArmSim(
                        gearbox,
                        gearRatio.getReduction(),
                        armMOI.in(KilogramSquareMeters),
                        armLength.in(Meters),
                        armMinAngle.in(Radians),
                        armMaxAngle.in(Radians),
                        isGravityCompensationEnabled,
                        armInitialAngle.in(Radians),
                        0.00153 // Measurement noise: ~1 tick of a 4096 CPR encoder
                        );

        updatePose();
    }

    /**
     * Steps the simulation forward in time.
     *
     * @param dt The timestep in seconds (typically 0.02 for the standard robot loop).
     * @param volts The applied motor voltage.
     */
    public void update(double dt, Voltage volts) {
        armSim.setInputVoltage(volts.in(Volts));
        armSim.update(dt);
        updatePose();
    }

    /**
     * Internal method to calculate the 3D pose of the mechanism based on the physics state and
     * designated axis.
     */
    private void updatePose() {
        double angleRads = armSim.getAngle();
        switch (axisOfRotation) {
            case X_AXIS:
                this.pose3d = new Pose3d(0, 0, 0, Rotation3d.fromRadians(angleRads, 0, 0));
                break;
            case Y_AXIS:
                this.pose3d = new Pose3d(0, 0, 0, Rotation3d.fromRadians(0, angleRads, 0));
                break;
            case Z_AXIS:
                this.pose3d = new Pose3d(0, 0, 0, Rotation3d.fromRadians(0, 0, angleRads));
                break;
        }
    }

    /** Syncs physics state to a SparkMax (expects velocity in RPM). */
    public void syncMotors(SparkMaxSim motor) {
        motor.getRelativeEncoderSim().setPosition(getMotorPositionRotations());
        motor.getRelativeEncoderSim().setVelocity(getMotorVelocityRPM());
    }

    /** Syncs physics state to a leader and follower SparkMax. */
    public void syncMotors(SparkMaxSim motor, SparkMaxSim motor2) {
        syncMotors(motor);
        syncMotors(motor2);
    }

    /** Syncs physics state to a Phoenix 6 TalonFX (expects velocity in RPS). */
    public void syncMotors(TalonFXSimState motor) {
        motor.setRawRotorPosition(getMotorPositionRotations());
        motor.setRotorVelocity(getMotorVelocityRPS());
    }

    /** Syncs physics state to a leader and follower TalonFX. */
    public void syncMotors(TalonFXSimState motor, TalonFXSimState motor2) {
        syncMotors(motor);
        syncMotors(motor2);
    }

    public Pose3d getPose() {
        return pose3d;
    }

    // --- Unit Math Helpers ---

    private double getMotorPositionRotations() {
        return gearRatio.getMotorPosition(Radians.of(armSim.getAngle())).in(Rotations);
    }

    private double getMotorVelocityRPM() {
        // Rad/s -> Mechanism Rot/s -> RPM -> Motor RPM
        return Units.radiansToRotations(armSim.getVelocity()) * 60.0 * gearRatio.getReduction();
    }

    private double getMotorVelocityRPS() {
        // Rad/s -> Mechanism Rot/s -> Motor Rot/s (Phoenix 6 standard)
        return Units.radiansToRotations(armSim.getVelocity()) * gearRatio.getReduction();
    }

    /** Returns the simulated current draw of the motor(s) in Amps. */
    public double getCurrentDrawAmps() {
        return armSim.getCurrentDraw();
    }
}
