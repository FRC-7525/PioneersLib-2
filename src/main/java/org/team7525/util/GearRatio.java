package org.team7525.util;

import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;

/** Represents a mechanical gear reduction to convert units between a motor and a mechanism. */
public class GearRatio {
    private final double reduction;

    /**
     * Creates a new GearRatio using a known reduction multiplier.
     *
     * @param reduction The reduction ratio (e.g., 5.0 for a 5:1 planetary gearbox where the motor
     *     turns 5 times for every 1 mechanism turn).
     */
    public GearRatio(double reduction) {
        if (!Double.isFinite(reduction) || reduction <= 0.0) {
            throw new IllegalArgumentException("Reduction must be finite and greater than zero");
        }
        this.reduction = reduction;
    }

    /**
     * Creates a new GearRatio by calculating the reduction from gear teeth.
     *
     * @param drivingTeeth The number of teeth on the driving gear (attached to motor).
     * @param drivenTeeth The number of teeth on the driven gear (attached to mechanism).
     */
    public GearRatio(int drivingTeeth, int drivenTeeth) {
        if (drivingTeeth <= 0 || drivenTeeth <= 0) {
            throw new IllegalArgumentException("Gear tooth counts must be greater than zero");
        }
        this.reduction = (double) drivenTeeth / drivingTeeth;
    }

    /**
     * Chains multiple gear stages together.
     *
     * @param nextStage The next gear ratio in the physical sequence.
     * @return A new GearRatio representing the total combined reduction.
     */
    public GearRatio combine(GearRatio nextStage) {
        return new GearRatio(this.reduction * nextStage.getReduction());
    }

    /**
     * @return The raw reduction multiplier (Motor Rotations / Mechanism Rotations).
     */
    public double getReduction() {
        return reduction;
    }

    // =======================================================================
    // WPILib Units Conversions
    // =======================================================================

    /** Converts a motor position into the physical mechanism position. */
    public Angle getMechanismPosition(Angle motorPosition) {
        return motorPosition.div(reduction);
    }

    /** Converts a target mechanism position into the required motor position. */
    public Angle getMotorPosition(Angle mechanismPosition) {
        return mechanismPosition.times(reduction);
    }

    /** Converts a motor velocity into the physical mechanism velocity. */
    public AngularVelocity getMechanismVelocity(AngularVelocity motorVelocity) {
        return motorVelocity.div(reduction);
    }

    /** Converts a target mechanism velocity into the required motor velocity. */
    public AngularVelocity getMotorVelocity(AngularVelocity mechanismVelocity) {
        return mechanismVelocity.times(reduction);
    }
}
