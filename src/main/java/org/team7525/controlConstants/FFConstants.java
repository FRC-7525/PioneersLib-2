package org.team7525.controlConstants;

/** Feedforward constants used to create feedforward controllers. */
public class FFConstants {

    private final LoggedTunableNumber kV, kA, kS, kG;

    public FFConstants(
            String baseString, double kV, double kA, double kS, double kG, boolean tunable) {
        this.kV = new LoggedTunableNumber(baseString + "/kV", tunable, kV);
        this.kA = new LoggedTunableNumber(baseString + "/kA", tunable, kA);
        this.kS = new LoggedTunableNumber(baseString + "/kS", tunable, kS);
        this.kG = new LoggedTunableNumber(baseString + "/kG", tunable, kG);
    }

    public FFConstants(String name, double kV, double kA, double kS, boolean tunable) {
        this(name, kV, kA, kS, 0, tunable);
    }

    public FFConstants(String name, double kV, double kA, boolean tunable) {
        this(name, kV, kA, 0, 0, tunable);
    }

    public FFConstants(String name, double kV, boolean tunable) {
        this(name, kV, 0, 0, 0, tunable);
    }

    public double getKV() {
        return kV.get();
    }

    public double getKA() {
        return kA.get();
    }

    public double getKS() {
        return kS.get();
    }

    public double getKG() {
        return kG.get();
    }

    public boolean hasChanged() {
        boolean changed = kV.hasChanged(hashCode());
        changed |= kA.hasChanged(hashCode());
        changed |= kS.hasChanged(hashCode());
        changed |= kG.hasChanged(hashCode());
        return changed;
    }
}
