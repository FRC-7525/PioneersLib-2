// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package org.team7525.subsystem.genericRoller;

import java.util.function.BooleanSupplier;
import lombok.Setter;
import org.littletonrobotics.junction.Logger;
import org.team7525.subsystem.ManagedSubsystem;
import org.team7525.subsystem.genericRoller.RollerSystemIO.RollerSystemIOMode;
import org.team7525.subsystem.genericRoller.RollerSystemIO.RollerSystemIOOutputs;
import org.wpilib.driverstation.RobotState;
import org.wpilib.math.filter.Debouncer;
import org.wpilib.util.Alert;

public class RollerSystem extends ManagedSubsystem<RollerSystemStates> {
    private final String inputsName;
    private final RollerSystemIO io;
    protected final RollerSystemIOInputsAutoLogged inputs = new RollerSystemIOInputsAutoLogged();
    private final RollerSystemIOOutputs outputs = new RollerSystemIOOutputs();

    private double kS = 0.0;
    private double kV = 0.0;
    private double dt = 0.02;

    @Setter private BooleanSupplier coastOverride = () -> false;

    private final Debouncer motorConnectedDebouncer =
            new Debouncer(0.5, Debouncer.DebounceType.FALLING);
    private final Debouncer followerMotorConnectedDebouncer =
            new Debouncer(0.5, Debouncer.DebounceType.FALLING);
    private final Alert disconnected;
    private final Alert followerDisconnected;

    public RollerSystem(
            String name, String inputsName, RollerSystemIO io, double kP, double kD, double dt) {
        super(name, RollerSystemStates.COAST);
        this.inputsName = inputsName;
        this.io = io;

        disconnected =
                new Alert(
                        name + " motor disconnected!",
                        name + "motor disconnected!",
                        Alert.Level.HIGH);
        followerDisconnected =
                new Alert(
                        name + " follower motor disconnected!",
                        name + "follower motor disconnected!",
                        Alert.Level.HIGH);
        outputs.kP = kP;
        outputs.kD = kD;
        this.dt = dt;
    }

    public RollerSystem(String name, String inputsName, RollerSystemIO io, double dt) {
        super(name, RollerSystemStates.COAST);
        this.inputsName = inputsName;
        this.io = io;
        this.dt = dt;

        disconnected =
                new Alert(
                        name + " motor disconnected!",
                        name + "motor disconnected!",
                        Alert.Level.HIGH);
        followerDisconnected =
                new Alert(
                        name + " follower motor disconnected!",
                        name + "follower motor disconnected!",
                        Alert.Level.HIGH);
    }

    public void periodicAfterScheduler() {
        io.applyOutputs(outputs);
    }

    public void runOpenLoop(double volts) {
        outputs.mode = RollerSystemIOMode.VOLTAGE_CONTROL;
        outputs.appliedVoltage = volts;
    }

    public void runClosedLoop(double setpointVelocity) {
        outputs.mode = RollerSystemIOMode.CLOSED_LOOP;
        outputs.velocity = setpointVelocity;
        outputs.feedforward = Math.signum(setpointVelocity) * kS + setpointVelocity * kV;
    }

    public void setGains(double kP, double kD) {
        outputs.kP = kP;
        outputs.kD = kD;
    }

    public void setFeedforward(double kS, double kV) {
        this.kS = kS;
        this.kV = kV;
    }

    public double getTorqueCurrent() {
        return inputs.torqueCurrentAmps;
    }

    public double getVelocity() {
        return inputs.velocityRadsPerSec;
    }

    public void stop() {
        outputs.mode = RollerSystemIOMode.BRAKE;
    }

    @Override
    protected void runState() {
        io.updateInputs(inputs, dt);
        Logger.processInputs(inputsName, inputs);
        disconnected.set(!motorConnectedDebouncer.calculate(inputs.connected));
        followerDisconnected.set(
                inputs.hasFollower
                        && !followerMotorConnectedDebouncer.calculate(inputs.followerConnected));

        // Update mode
        if (RobotState.isDisabled()) {
            outputs.mode = RollerSystemIOMode.BRAKE;

            if (coastOverride.getAsBoolean()) {
                outputs.mode = RollerSystemIOMode.COAST;
            }
        }
    }
}
