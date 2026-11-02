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
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.LinearSystem;
import org.wpilib.math.system.Models;
import org.wpilib.math.util.Units;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.units.measure.MomentOfInertia;
import org.wpilib.units.measure.Voltage;

/**
 * A simplified wrapper for simulating an FRC shooter flywheel. Manages rotational velocity physics,
 * manually integrates position for 3D visualization, tracks current draw, and synchronizes to
 * SparkMax and TalonFX simulated controllers.
 */
public class SimpleFlywheel {
    private final FlywheelSim flywheelSim;
    private final GearRatio gearRatio;
    private final Axis axisOfRotation;

    private Pose3d pose3d;
    private double simulatedPositionRads = 0.0;

    /**
     * Constructs a new SimpleFlywheel physics simulation using a custom LinearSystem plant. Ideal
     * for using characterized System Identification (SysId) data.
     *
     * @param plant The mathematical plant model representing the flywheel's velocity dynamics.
     * @param gearbox The physical motor configuration (e.g. DCMotor.NEO(2)).
     * @param gearRatio The mechanical gear reduction from the motors to the flywheel.
     * @param axisOfRotation The 3D axis the flywheel spins around (for AdvantageScope).
     */
    public SimpleFlywheel(
            LinearSystem<N1, N1, N1> plant,
            DCMotor gearbox,
            GearRatio gearRatio,
            Axis axisOfRotation) {
        this.gearRatio = gearRatio;
        this.axisOfRotation = axisOfRotation;

        flywheelSim = new FlywheelSim(plant, gearbox, gearRatio.getReduction(), 0.1);

        updatePose();
    }

    /**
     * Steps the simulation forward in time.
     *
     * @param dt The timestep in seconds (typically 0.02).
     * @param volts The applied motor voltage.
     */
    public void update(double dt, Voltage volts) {
        flywheelSim.setInputVoltage(volts.in(Volts));
        flywheelSim.update(dt);

        // WPILib's FlywheelSim only tracks velocity. We manually integrate it
        // to get a continuous position for motor sync and 3D visualization.
        simulatedPositionRads += flywheelSim.getAngularVelocity() * dt;

        updatePose();
    }

    /** Internal method to calculate the 3D pose of the spinning wheel. */
    private void updatePose() {
        switch (axisOfRotation) {
            case X_AXIS:
                this.pose3d =
                        new Pose3d(0, 0, 0, Rotation3d.fromRadians(simulatedPositionRads, 0, 0));
                break;
            case Y_AXIS:
                this.pose3d =
                        new Pose3d(0, 0, 0, Rotation3d.fromRadians(0, simulatedPositionRads, 0));
                break;
            case Z_AXIS:
                this.pose3d =
                        new Pose3d(0, 0, 0, Rotation3d.fromRadians(0, 0, simulatedPositionRads));
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

    /**
     * Returns the simulated current draw of the motor(s) in Amps. Useful for central battery
     * brownout simulation.
     */
    public double getCurrentDrawAmps() {
        return flywheelSim.getCurrentDraw();
    }

    // --- Unit Math Helpers ---

    private double getMotorPositionRotations() {
        return gearRatio.getMotorPosition(Radians.of(simulatedPositionRads)).in(Rotations);
    }

    private double getMotorVelocityRPM() {
        // Rad/s -> Mechanism Rot/s -> RPM -> Motor RPM
        return Units.radiansToRotations(flywheelSim.getAngularVelocity())
                * 60.0
                * gearRatio.getReduction();
    }

    private double getMotorVelocityRPS() {
        // Rad/s -> Mechanism Rot/s -> Motor Rot/s (Phoenix 6 standard)
        return Units.radiansToRotations(flywheelSim.getAngularVelocity())
                * gearRatio.getReduction();
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
