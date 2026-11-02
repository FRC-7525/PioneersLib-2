package org.team7525.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.wpilib.units.Units.Radians;

import org.junit.jupiter.api.Test;

class GearRatioTest {
    @Test
    void convertsBetweenMotorAndMechanismPosition() {
        GearRatio ratio = new GearRatio(5.0);

        assertEquals(1.0, ratio.getMechanismPosition(Radians.of(5.0)).in(Radians), 1e-9);
        assertEquals(5.0, ratio.getMotorPosition(Radians.of(1.0)).in(Radians), 1e-9);
    }

    @Test
    void rejectsInvalidRatios() {
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(0.0));
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(-1.0));
        assertThrows(IllegalArgumentException.class, () -> new GearRatio(0, 10));
    }
}
