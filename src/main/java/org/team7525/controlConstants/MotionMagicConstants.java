package org.team7525.controlConstants;

/** Motion Magic constants used to configure Motion Magic controllers */
public class MotionMagicConstants {

    private final LoggedTunableNumber kCruiseVelocity, kMaxAcceleration, kJerk, kExpoKV, kExpoKA;

    public MotionMagicConstants(
            String baseString,
            double kCruiseVelocity,
            double kMaxAcceleration,
            double kJerk,
            double kExpoKV,
            double kExpoKA,
            boolean tunable) {
        this.kCruiseVelocity =
                new LoggedTunableNumber(baseString + "/kCruiseVelocity", tunable, kCruiseVelocity);
        this.kMaxAcceleration =
                new LoggedTunableNumber(
                        baseString + "/kMaxAcceleration", tunable, kMaxAcceleration);
        this.kJerk = new LoggedTunableNumber(baseString + "/kJerk", tunable, kJerk);
        this.kExpoKV = new LoggedTunableNumber(baseString + "/kExpoKV", tunable, kExpoKV);
        this.kExpoKA = new LoggedTunableNumber(baseString + "/kExpoKA", tunable, kExpoKA);
    }

    public double getKCruiseVelocity() {
        return kCruiseVelocity.get();
    }

    public double getKMaxAcceleration() {
        return kMaxAcceleration.get();
    }

    public double getKJerk() {
        return kJerk.get();
    }

    public double getKExpoKV() {
        return kExpoKV.get();
    }

    public double getKExpoKA() {
        return kExpoKA.get();
    }

    public boolean hasChanged() {
        boolean changed = kCruiseVelocity.hasChanged(hashCode());
        changed |= kMaxAcceleration.hasChanged(hashCode());
        changed |= kJerk.hasChanged(hashCode());
        changed |= kExpoKV.hasChanged(hashCode());
        changed |= kExpoKA.hasChanged(hashCode());
        return changed;
    }
}
