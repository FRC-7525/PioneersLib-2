// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package org.team7525.subsystem.genericRoller;

import org.wpilib.driverstation.RobotState;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.simulation.DCMotorSim;

public class RollerSystemIOSim implements RollerSystemIO {
    private final DCMotorSim sim;
    private final DCMotor gearbox;
    private double appliedVoltage = 0.0;
    private boolean hasFollower;
    private double measuredVelocity;
    private double dt = 0.02;
    private double previousError;

    public RollerSystemIOSim(
            DCMotor motorModel, double reduction, double moi, boolean hasFollower) {
        gearbox = motorModel;
        sim =
                new DCMotorSim(
                        Models.singleJointedArmFromPhysicalConstants(motorModel, moi, reduction),
                        motorModel);
        this.hasFollower = hasFollower;
    }

    @Override
    public void updateInputs(RollerSystemIOInputs inputs, double dt) {
        this.dt = dt;
        sim.update(dt);

        inputs.connected = true;
        if (hasFollower) {
            inputs.followerConnected = true;
            inputs.hasFollower = true;
        }
        inputs.positionRads = sim.getAngularPosition();
        inputs.velocityRadsPerSec = sim.getAngularVelocity();
        measuredVelocity = sim.getAngularVelocity();
        inputs.appliedVoltage = appliedVoltage;
        inputs.supplyCurrentAmps = sim.getCurrentDraw();
        inputs.torqueCurrentAmps = gearbox.getCurrent(sim.getAngularVelocity(), appliedVoltage);
        inputs.tempCelsius = 0.0;
    }

    @Override
    public void applyOutputs(RollerSystemIOOutputs outputs) {

        if (RobotState.isDisabled()
                || outputs.mode == RollerSystemIOMode.BRAKE
                || outputs.mode == RollerSystemIOMode.COAST) {
            appliedVoltage = 0.0;
        } else if (outputs.mode == RollerSystemIOMode.CLOSED_LOOP) {
            double error = outputs.velocity - measuredVelocity;
            double derivative = (error - previousError) / dt;
            appliedVoltage = outputs.kP * error + outputs.kD * derivative + outputs.feedforward;
            previousError = error;
        } else {
            appliedVoltage = outputs.appliedVoltage;
        }
        appliedVoltage = Math.clamp(appliedVoltage, -12.0, 12.0);
        sim.setInputVoltage(appliedVoltage);
    }
}
