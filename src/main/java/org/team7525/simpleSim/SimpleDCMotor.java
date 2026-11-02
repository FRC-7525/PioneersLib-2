package org.team7525.simpleSim;

import static org.wpilib.units.Units.KilogramSquareMeters;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.Rotations;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.sim.TalonFXSimState;
import com.revrobotics.sim.SparkMaxSim;
import org.team7525.util.GearRatio;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.numbers.N1;
import org.wpilib.math.numbers.N2;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.LinearSystem;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.DCMotorSim;
import org.wpilib.units.measure.MomentOfInertia;
import org.wpilib.units.measure.Voltage;

/**
 * A simplified wrapper for simulating generic unconstrained rotating mechanisms (like intakes,
 * indexers, or conveyor belts). Manages physics, 3D odometry updates, current tracking, and
 * unit-correct synchronization to REV SparkMax and CTRE TalonFX simulated motor controllers.
 */
public class SimpleDCMotor {
    private final DCMotorSim dcMotorSim;
    private final GearRatio gearRatio;
    private final Axis axisOfRotation;
    private Pose3d pose3d;

    /**
     * Constructs a new SimpleDCMotor physics simulation using a custom LinearSystem plant. Ideal
     * for using characterized System Identification (SysId) data.
     *
     * @param plant The 2-state plant model (Position and Velocity) representing the dynamics.
     * @param gearbox The physical motor configuration (e.g. DCMotor.NEO(1)).
     * @param gearRatio The mechanical gear reduction from the motor to the mechanism.
     * @param axisOfRotation The 3D axis the mechanism spins around.
     */
    public SimpleDCMotor(
            LinearSystem<N2, N1, N2> plant,
            DCMotor gearbox,
            GearRatio gearRatio,
            Axis axisOfRotation) {
        this.gearRatio = gearRatio;
        this.axisOfRotation = axisOfRotation;

        dcMotorSim = new DCMotorSim(plant, gearbox, gearRatio.getReduction(), 0.00153, 0.1);

        updatePose();
    }

    /**
     * Steps the simulation forward in time.
     *
     * @param dt The timestep in seconds (typically 0.02).
     * @param volts The applied motor voltage.
     */
    public void update(double dt, Voltage volts) {
        dcMotorSim.setInputVoltage(volts.in(Volts));
        dcMotorSim.update(dt);
        updatePose();
    }

    /** Internal method to calculate the 3D pose of the spinning mechanism. */
    private void updatePose() {
        double angleRads = dcMotorSim.getAngularPosition();
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
        motor2.getRelativeEncoderSim().setPosition(motor.getRelativeEncoderSim().getPosition());
        motor2.getRelativeEncoderSim().setVelocity(motor.getRelativeEncoderSim().getVelocity());
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

    /** Returns the simulated current draw of the motor(s) in Amps. */
    public double getCurrentDrawAmps() {
        return dcMotorSim.getCurrentDraw();
    }

    // --- Unit Math Helpers ---

    private double getMotorPositionRotations() {
        return gearRatio
                .getMotorPosition(Radians.of(dcMotorSim.getAngularPosition()))
                .in(Rotations);
    }

    private double getMotorVelocityRPM() {
        return Units.radiansToRotations(dcMotorSim.getAngularVelocity())
                * 60.0
                * gearRatio.getReduction();
    }

    private double getMotorVelocityRPS() {
        return Units.radiansToRotations(dcMotorSim.getAngularVelocity()) * gearRatio.getReduction();
    }

    public LinearSystem<N1, N1, N1> getSysIDLinearSystem(double kv, double kA) {
        return Models.flywheelFromSysId(kv, kA);
    }

    public LinearSystem<N1, N1, N1> getPhysicalConstantsLinearSystem(
            DCMotor motor, MomentOfInertia moi, GearRatio gearRatio) {
        return Models.flywheelFromPhysicalConstants(
                motor, moi.in(KilogramSquareMeters), gearRatio.getReduction());
    }
}
