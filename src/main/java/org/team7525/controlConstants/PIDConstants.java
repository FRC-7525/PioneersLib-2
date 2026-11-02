package org.team7525.controlConstants;

import org.wpilib.telemetry.TelemetryLoggable;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.tunable.TunableDouble;
import org.wpilib.tunable.Tunables;

/** PID constants used to create PID controllers */
public class PIDConstants implements TelemetryLoggable {

    private final boolean tunable;
    private final double defaultKP, defaultKI, defaultKD, defaultIZone;
    private final TunableDouble tKP, tKI, tKD, tIZone;

    public PIDConstants(
            String name, double kP, double kI, double kD, double iZone, boolean tunable) {
        this.tunable = tunable;
        this.defaultKP = kP;
        this.defaultKI = kI;
        this.defaultKD = kD;
        this.defaultIZone = iZone;

        if (tunable) {
            tKP = Tunables.addDouble(name + "/kP", kP);
            tKI = Tunables.addDouble(name + "/kI", kI);
            tKD = Tunables.addDouble(name + "/kD", kD);
            tIZone = Tunables.addDouble(name + "/iZone", iZone);
        } else {
            tKP = tKI = tKD = tIZone = null;
        }
    }

    public PIDConstants(String name, double kP, double kI, double kD, boolean tunable) {
        this(name, kP, kI, kD, 1.0, tunable);
    }

    public PIDConstants(String name, double kP, double kD, boolean tunable) {
        this(name, kP, 0, kD, tunable);
    }

    public PIDConstants(String name, double kP, boolean tunable) {
        this(name, kP, 0, 0, tunable);
    }

    public double getKP() {
        return tunable ? tKP.get() : defaultKP;
    }

    public double getKI() {
        return tunable ? tKI.get() : defaultKI;
    }

    public double getKD() {
        return tunable ? tKD.get() : defaultKD;
    }

    public double getIZone() {
        return tunable ? tIZone.get() : defaultIZone;
    }

    @Override
    public void logTo(TelemetryTable table) {
        table.log("kP", getKP());
        table.log("kI", getKI());
        table.log("kD", getKD());
        table.log("iZone", getIZone());
    }
}
