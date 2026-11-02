package org.team7525.controlConstants;

import org.wpilib.telemetry.TelemetryLoggable;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.tunable.TunableDouble;
import org.wpilib.tunable.Tunables;

public class FFConstants implements TelemetryLoggable {
    private final boolean tunable;

    // Defaults / initial values
    private final double defaultKS, defaultKV, defaultKA, defaultKG;

    // Tunable handles (null if not tunable)
    private final TunableDouble tKS, tKV, tKA, tKG;

    public FFConstants(String name, double ks, double kg, double kv, double ka, boolean tunable) {
        this.tunable = tunable;
        this.defaultKS = ks;
        this.defaultKV = kv;
        this.defaultKA = ka;
        this.defaultKG = kg;

        if (tunable) {
            tKS = Tunables.addDouble(name + "/kS", ks);
            tKV = Tunables.addDouble(name + "/kV", kv);
            tKA = Tunables.addDouble(name + "/kA", ka);
            tKG = Tunables.addDouble(name + "/kG", kg);
        } else {
            tKS = tKV = tKA = tKG = null;
        }
    }

    public FFConstants(String name, double ks, double kv, double ka, boolean tunable) {
        this(name, ks, 0, kv, ka, tunable);
    }

    public FFConstants(String name, double ks, double kv, boolean tunable) {
        this(name, ks, 0, kv, 0, tunable);
    }

    public FFConstants(String name, double kv, boolean tunable) {
        this(name, 0, 0, kv, 0, tunable);
    }

    // --- Getters (read live tunable value if tunable, else the constant) ---

    public double getKS() {
        return tunable ? tKS.get() : defaultKS;
    }

    public double getKV() {
        return tunable ? tKV.get() : defaultKV;
    }

    public double getKA() {
        return tunable ? tKA.get() : defaultKA;
    }

    public double getKG() {
        return tunable ? tKG.get() : defaultKG;
    }

    @Override
    public void logTo(TelemetryTable table) {
        table.log("kS", getKS());
        table.log("kV", getKV());
        table.log("kA", getKA());
        table.log("kG", getKG());
    }
}
