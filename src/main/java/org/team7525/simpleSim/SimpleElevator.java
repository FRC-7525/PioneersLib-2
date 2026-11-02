package org.team7525.simpleSim;

import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.sim.TalonFXSimState;
import com.revrobotics.sim.SparkMaxSim;
import org.team7525.util.GearRatio;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.simulation.ElevatorSim;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.Mass;
import org.wpilib.units.measure.Voltage;

/**
 * A simplified wrapper for simulating a multi-stage linear FRC elevator. Models the overall physics
 * load natively while extrapolating the kinematic positions of individual stages for 3D
 * visualization based on rigging type.
 */
public class SimpleElevator {
    public enum RiggingType {
        /**
         * All stages move simultaneously. If the carriage moves 2 inches, intermediate stages move
         * 1 inch.
         */
        CASCADING,
        /**
         * Stages move sequentially. The carriage extends fully before the next stage begins to
         * rise.
         */
        CONTINUOUS
    }

    private final ElevatorSim elevatorSim;
    private final GearRatio gearRatio;
    private final Distance drumRadius;
    private final Axis axisOfMovement;

    // Multi-stage kinematics
    private final RiggingType riggingType;
    private final int numMovingParts;
    private final double maxStageExtensionMeters;
    private Pose3d[] stagePoses;

    /**
     * Constructs a new SimpleElevator physics simulation.
     *
     * @param gearbox The physical motor configuration (e.g. DCMotor.NEO(2)).
     * @param gearRatio The mechanical gear reduction from the motors to the drum.
     * @param totalCarriageMass The physical mass of ALL moving stages combined.
     * @param drumRadius The radius of the spool/drum driving the belt or chain.
     * @param minHeight The minimum physical height limit of the final carriage.
     * @param maxHeight The maximum physical height limit of the final carriage.
     * @param initialHeight The starting height of the final carriage upon boot.
     * @param simulateGravity Whether to simulate gravity acting on the carriage mass.
     * @param axisOfMovement The 3D axis the elevator translates along (usually Z_AXIS).
     * @param riggingType Cascading (simultaneous) or Continuous (sequential).
     * @param numMovingParts Total moving stages (e.g., 1 intermediate stage + 1 carriage = 2).
     */
    public SimpleElevator(
            DCMotor gearbox,
            GearRatio gearRatio,
            Mass totalCarriageMass,
            Distance drumRadius,
            Distance minHeight,
            Distance maxHeight,
            Distance initialHeight,
            boolean simulateGravity,
            Axis axisOfMovement,
            RiggingType riggingType,
            int numMovingParts) {
        if (numMovingParts <= 0) {
            throw new IllegalArgumentException("numMovingParts must be greater than zero");
        }
        if (maxHeight.in(Meters) <= minHeight.in(Meters)) {
            throw new IllegalArgumentException("maxHeight must be greater than minHeight");
        }
        this.gearRatio = gearRatio;
        this.drumRadius = drumRadius;
        this.axisOfMovement = axisOfMovement;
        this.riggingType = riggingType;
        this.numMovingParts = numMovingParts;
        this.stagePoses = new Pose3d[numMovingParts];

        // The max extension of a single stage is total travel divided by number of moving parts
        this.maxStageExtensionMeters =
                (maxHeight.in(Meters) - minHeight.in(Meters)) / numMovingParts;
        elevatorSim =
                new ElevatorSim(
                        gearbox,
                        gearRatio.getReduction(),
                        totalCarriageMass.in(Kilograms),
                        drumRadius.in(Meters),
                        minHeight.in(Meters),
                        maxHeight.in(Meters),
                        simulateGravity,
                        initialHeight.in(Meters),
                        0.001 // Measurement noise: 1 millimeter standard deviation
                        );

        updatePoses();
    }

    public void update(double dt, Voltage volts) {
        elevatorSim.setInputVoltage(volts.in(Volts));
        elevatorSim.update(dt);
        updatePoses();
    }

    /**
     * Calculates the individual 3D pose of every moving stage. Index 0 is the first intermediate
     * stage, Index (n-1) is the final carriage.
     */
    private void updatePoses() {
        double totalHeight = elevatorSim.getPosition();

        for (int i = 0; i < numMovingParts; i++) {
            double stageAbsoluteHeight = 0;

            if (riggingType == RiggingType.CASCADING) {
                // Stages move proportionally based on their position in the stack
                stageAbsoluteHeight = totalHeight * ((i + 1.0) / numMovingParts);
            } else if (riggingType == RiggingType.CONTINUOUS) {
                // The final carriage (highest index) maxes out first, pulling the lower stages up
                // sequentially
                int stagesAbove = (numMovingParts - 1) - i;
                double deadzone = maxStageExtensionMeters * stagesAbove;
                stageAbsoluteHeight = Math.max(0, totalHeight - deadzone);
            }

            stagePoses[i] = generatePoseForAxis(stageAbsoluteHeight);
        }
    }

    private Pose3d generatePoseForAxis(double heightMeters) {
        switch (axisOfMovement) {
            case X_AXIS:
                return new Pose3d(heightMeters, 0, 0, new Rotation3d());
            case Y_AXIS:
                return new Pose3d(0, heightMeters, 0, new Rotation3d());
            case Z_AXIS:
                return new Pose3d(0, 0, heightMeters, new Rotation3d());
            default:
                return new Pose3d();
        }
    }

    /**
     * Returns an array of Pose3d objects for 3D visualization. Pass this array directly into
     * AdvantageScope or Field3d.
     */
    public Pose3d[] getStagePoses() {
        return stagePoses;
    }

    // --- Motor Sync Methods (Unchanged) ---

    public void syncMotors(SparkMaxSim motor) {
        motor.getRelativeEncoderSim().setPosition(getMotorPositionRotations());
        motor.getRelativeEncoderSim().setVelocity(getMotorVelocityRPM());
    }

    public void syncMotors(TalonFXSimState motor) {
        motor.setRawRotorPosition(getMotorPositionRotations());
        motor.setRotorVelocity(getMotorVelocityRPS());
    }

    public double getCurrentDrawAmps() {
        return elevatorSim.getCurrentDraw();
    }

    // --- Unit Math Helpers ---

    private double getMechanismRotations() {
        return elevatorSim.getPosition() / (2 * Math.PI * drumRadius.in(Meters));
    }

    private double getMotorPositionRotations() {
        return getMechanismRotations() * gearRatio.getReduction();
    }

    private double getMotorVelocityRPM() {
        return (elevatorSim.getVelocity() / (2 * Math.PI * drumRadius.in(Meters)))
                * 60.0
                * gearRatio.getReduction();
    }

    private double getMotorVelocityRPS() {
        return (elevatorSim.getVelocity() / (2 * Math.PI * drumRadius.in(Meters)))
                * gearRatio.getReduction();
    }
}
