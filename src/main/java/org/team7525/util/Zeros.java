package org.team7525.util;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Hertz;
import static org.wpilib.units.Units.MetersPerSecond;
import static org.wpilib.units.Units.Seconds;
import static org.wpilib.units.Units.Volts;

import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Frequency;
import org.wpilib.units.measure.LinearVelocity;
import org.wpilib.units.measure.Time;
import org.wpilib.units.measure.Voltage;

public final class Zeros {
    public static final double ZERO_DOUBLE = 0.0;
    public static final int ZERO_INT = 0;
    public static final Voltage ZERO_VOLTAGE = Volts.of(0.0);
    public static final Frequency ZERO_FREQUENCY = Hertz.of(0.0);
    public static final LinearVelocity ZERO_LINEAR_VELOCITY = MetersPerSecond.of(0.0);
    public static final Angle ZERO_ANGLE = Degrees.of(0.0);
    public static final String EMPTY_STRING = "";
    public static final Current ZERO_CURRENT = Amps.of(0.0);
    public static final Time ZERO_TIME = Seconds.of(0.0);

    public static SwerveModulePosition[] zeroModulePositions() {
        return new SwerveModulePosition[] {
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition(),
            new SwerveModulePosition()
        };
    }
}
