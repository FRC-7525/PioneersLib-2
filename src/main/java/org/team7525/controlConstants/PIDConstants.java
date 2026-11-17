package org.team7525.controlConstants;

/** PID constants used to create PID controllers */
public class PIDConstants {

    private final LoggedTunableNumber kP, kI, kD, iZone;

    public PIDConstants(
            String baseString, double kP, double kI, double kD, double iZone, boolean tunable) {
        this.kP = new LoggedTunableNumber(baseString + "/kP", tunable, kP);
        this.kI = new LoggedTunableNumber(baseString + "/kI", tunable, kI);
        this.kD = new LoggedTunableNumber(baseString + "/kD", tunable, kD);
        this.iZone = new LoggedTunableNumber(baseString + "/iZone", tunable, iZone);
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
        return kP.get();
    }

    public double getKI() {
        return kI.get();
    }

    public double getKD() {
        return kD.get();
    }

    public double getIZone() {
        return iZone.get();
    }

    public boolean hasChanged() {
        boolean changed = kP.hasChanged(hashCode());
        changed |= kI.hasChanged(hashCode());
        changed |= kD.hasChanged(hashCode());
        changed |= iZone.hasChanged(hashCode());
        return changed;
    }
}
