package org.team7525.simpleSim;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;

import org.junit.jupiter.api.Test;
import org.team7525.util.GearRatio;
import org.wpilib.math.system.DCMotor;

class SimpleElevatorTest {
    @Test
    void rejectsInvalidStageCount() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new SimpleElevator(
                                DCMotor.getNEO(1),
                                new GearRatio(10),
                                Kilograms.of(5),
                                Meters.of(0.02),
                                Meters.of(0),
                                Meters.of(1),
                                Meters.of(0),
                                true,
                                Axis.Z_AXIS,
                                SimpleElevator.RiggingType.CASCADING,
                                0));
    }
}
