package org.team7525.simpleSim;

import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.MetersPerSecond;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.sim.TalonFXSimState;
import com.revrobotics.sim.SparkMaxSim;
import java.util.ArrayList;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;
import org.team7525.util.GearRatio;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.system.DCMotor;
import org.wpilib.simulation.ElevatorSim;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.LinearVelocity;
import org.wpilib.units.measure.Mass;
import org.wpilib.units.measure.Voltage;
import org.wpilib.util.Color8Bit;

/**
 * A simplified wrapper for simulating a multi-stage linear FRC elevator. Models the overall physics
 * load natively while extrapolating the kinematic positions of individual stages for 3D
 * visualization based on rigging type.
 */
public class SimpleElevatorSim {
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

    @SuppressWarnings("unused")
    private final Axis axisOfMovement; // Retained in case of future 3D transformations

    // Multi-stage kinematics
    private final RiggingType riggingType;
    private final int numMovingParts;
    private final double maxStageExtensionMeters;

    /* Mechanism2d visualization of the elevator */
    private final LoggedMechanism2d mech2d;
    private final LoggedMechanismLigament2d[] stageLigaments;

    /**
     * Constructs a new SimpleElevatorSim physics simulation.
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
    public SimpleElevatorSim(
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

        // The max extension of a single stage is total travel divided by number of moving parts
        this.maxStageExtensionMeters =
                (maxHeight.in(Meters) - minHeight.in(Meters)) / numMovingParts;

        this.elevatorSim =
                new ElevatorSim(
                        gearbox,
                        gearRatio.getReduction(),
                        totalCarriageMass.in(Kilograms),
                        drumRadius.in(Meters),
                        minHeight.in(Meters),
                        maxHeight.in(Meters),
                        simulateGravity,
                        initialHeight.in(Meters));

        // Configure Mechanism2d
        // Canvas is slightly taller than max height to prevent clipping
        this.mech2d = new LoggedMechanism2d(1.0, maxHeight.in(Meters) + 0.5);
        this.mech2d.setBackgroundColor(new Color8Bit(0, 0, 0));

        var root = mech2d.getRoot("ElevatorRoot", 0.5, 0.0); // Base at bottom center

        // Unify all ligaments into a single array for easier iteration
        this.stageLigaments = new LoggedMechanismLigament2d[numMovingParts];
        for (int i = 0; i < numMovingParts; i++) {
            // Uniquely name nodes to prevent WPILib duplicates ("Stage 1", "Stage 2", ...,
            // "Carriage")
            String stageName = (i == numMovingParts - 1) ? "Carriage" : "Stage " + (i + 1);
            stageLigaments[i] = root.append(new LoggedMechanismLigament2d(stageName, 0, 90));
        }

        updatePoses();
    }

    public void update(double dt, Voltage volts) {
        elevatorSim.setInputVoltage(volts.in(Volts));
        elevatorSim.update(dt);
        updatePoses();
    }

    public LoggedMechanism2d getMechanism2d() {
        return mech2d;
    }

    /**
     * Calculates the individual 3D pose of every moving stage. Index 0 is the first intermediate
     * stage, Index (n-1) is the final carriage.
     */
    private void updatePoses() {
        double totalHeight = elevatorSim.getPosition();

        for (int i = 0; i < numMovingParts; i++) {
            double stageAbsoluteHeight = 0;

            switch (riggingType) {
                case CASCADING -> {
                    // Stages move proportionally based on their position in the stack
                    stageAbsoluteHeight = totalHeight * ((i + 1.0) / numMovingParts);
                }
                case CONTINUOUS -> {
                    // The final carriage (highest index) maxes out first, pulling the lower stages
                    // up sequentially
                    int stagesAbove = (numMovingParts - 1) - i;
                    double deadzone = maxStageExtensionMeters * stagesAbove;
                    stageAbsoluteHeight = Math.max(0, totalHeight - deadzone);
                }
            }

            // IMPORTANT FIX: Apply the length AFTER calculating it
            stageLigaments[i].setLength(stageAbsoluteHeight);
        }
    }

    /**
     * Returns an array of Pose3d objects for 3D visualization. Pass this array directly into
     * AdvantageScope or Field3d.
     */
    public ArrayList<Pose3d> getStagePoses() {
        return mech2d.generate3dMechanism();
    }

    // --- Motor Sync Methods ---

    public void syncMotors(SparkMaxSim motor) {
        motor.getRelativeEncoderSim().setPosition(getMotorPositionRotations());
        motor.getRelativeEncoderSim().setVelocity(getMotorVelocityRPM());
    }

    public void syncMotors(TalonFXSimState motor) {
        motor.setRawRotorPosition(getMotorPositionRotations());
        motor.setRotorVelocity(getMotorVelocityRPS());
    }

    public Distance getCurrentHeight() {
        return Meters.of(elevatorSim.getPosition());
    }

    public LinearVelocity getCurrentVelocity() {
        return MetersPerSecond.of(elevatorSim.getVelocity());
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
