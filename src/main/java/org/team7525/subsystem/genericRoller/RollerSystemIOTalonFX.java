// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.

package org.team7525.subsystem.genericRoller;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Celsius;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import org.team7525.io.StatusSignalRefresher;
import org.wpilib.driverstation.RobotState;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Temperature;
import org.wpilib.units.measure.Voltage;

public class RollerSystemIOTalonFX implements RollerSystemIO {
    private final TalonFX motor;
    private final TalonFX followerMotor;
    private double appliedVoltage = 0.0;
    private boolean hasFollower;
    private double measuredVelocity;
    private double dt = 0.02;
    private double previousError;
    private boolean brakeModeEnabled = false;

    // Status Signals
    protected StatusSignal<Angle> position;
    protected StatusSignal<AngularVelocity> velocity;
    protected StatusSignal<Voltage> appliedVolts;
    protected StatusSignal<Current> torqueCurrent;
    protected StatusSignal<Current> supplyCurrent;
    protected StatusSignal<Temperature> temp;
    protected StatusSignal<Voltage> followerAppliedVolts;
    protected StatusSignal<Current> followerTorqueCurrent;
    protected StatusSignal<Current> followerSupplyCurrent;
    protected StatusSignal<Temperature> followerTemp;

    private final VoltageOut voltageOut = new VoltageOut(0.0).withUpdateFreqHz(0.0);

    public RollerSystemIOTalonFX(int id, CANBus bus, double reduction) {
        motor = new TalonFX(id, bus);
        followerMotor = null;
        this.hasFollower = false;
        setup();
    }

    public RollerSystemIOTalonFX(
            int id, int followerId, MotorAlignmentValue alignment, CANBus bus, double reduction) {
        motor = new TalonFX(id, bus);
        followerMotor = new TalonFX(followerId, bus);
        followerMotor.setControl(new Follower(id, alignment));
        this.hasFollower = true;
        setup();
    }

    private void setup() {
        position = motor.getPosition();
        velocity = motor.getVelocity();
        appliedVolts = motor.getMotorVoltage();
        torqueCurrent = motor.getTorqueCurrent();
        supplyCurrent = motor.getSupplyCurrent();
        temp = motor.getDeviceTemp();

        if (hasFollower) {
            followerAppliedVolts = followerMotor.getMotorVoltage();
            followerTorqueCurrent = followerMotor.getTorqueCurrent();
            followerSupplyCurrent = followerMotor.getSupplyCurrent();
            followerTemp = followerMotor.getDeviceTemp();
            BaseStatusSignal.setUpdateFrequencyForAll(
                    50,
                    position,
                    velocity,
                    appliedVolts,
                    torqueCurrent,
                    supplyCurrent,
                    temp,
                    followerAppliedVolts,
                    followerTorqueCurrent,
                    followerSupplyCurrent,
                    followerTemp);
            StatusSignalRefresher.getInstance()
                    .addStatusSignals(
                            position,
                            velocity,
                            appliedVolts,
                            torqueCurrent,
                            supplyCurrent,
                            temp,
                            followerAppliedVolts,
                            followerTorqueCurrent,
                            followerSupplyCurrent,
                            followerTemp);
        } else {
            BaseStatusSignal.setUpdateFrequencyForAll(
                    50, position, velocity, appliedVolts, torqueCurrent, supplyCurrent, temp);
            StatusSignalRefresher.getInstance()
                    .addStatusSignals(
                            position, velocity, appliedVolts, torqueCurrent, supplyCurrent, temp);
        }
    }

    @Override
    public void updateInputs(RollerSystemIOInputs inputs, double dt) {
        this.dt = dt;
        inputs.connected = motor.isAlive();
        inputs.positionRads = position.getValue().in(Radians);
        inputs.velocityRadsPerSec = velocity.getValue().in(RadiansPerSecond);
        measuredVelocity = inputs.velocityRadsPerSec;
        inputs.appliedVoltage = appliedVolts.getValue().in(Volts);
        inputs.supplyCurrentAmps = supplyCurrent.getValue().in(Amps);
        inputs.torqueCurrentAmps = torqueCurrent.getValue().in(Amps);
        inputs.tempCelsius = temp.getValue().in(Celsius);
        inputs.hasFollower = hasFollower;
        if (hasFollower) {
            inputs.followerConnected = followerMotor.isAlive();
            inputs.followerAppliedVoltage = followerAppliedVolts.getValue().in(Volts);
            inputs.followerSupplyCurrentAmps = followerSupplyCurrent.getValue().in(Amps);
            inputs.followerTorqueCurrentAmps = followerTorqueCurrent.getValue().in(Amps);
            inputs.followerTempCelsius = followerTemp.getValue().in(Celsius);
        }
    }

    @Override
    public void applyOutputs(RollerSystemIOOutputs outputs) {
        if (RobotState.isDisabled()
                || outputs.state == RollerSystemStates.BRAKE
                || outputs.state == RollerSystemStates.COAST) {
            previousError = 0.0;
            if (outputs.state == RollerSystemStates.COAST && brakeModeEnabled) {
                brakeModeEnabled = false;
                motor.getConfigurator()
                        .apply(new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast));
            } else if (outputs.state == RollerSystemStates.BRAKE && !brakeModeEnabled) {
                brakeModeEnabled = true;
                motor.getConfigurator()
                        .apply(new MotorOutputConfigs().withNeutralMode(NeutralModeValue.Brake));
            }
            appliedVoltage = 0.0;
        } else if (outputs.state == RollerSystemStates.CLOSED_LOOP) {
            double error = outputs.velocity - measuredVelocity;
            double derivative = (error - previousError) / dt;
            appliedVoltage = outputs.kP * error + outputs.kD * derivative + outputs.feedforward;
            previousError = error;
        } else {
            previousError = 0.0;
            appliedVoltage = outputs.appliedVoltage;
        }
        appliedVoltage = Math.clamp(appliedVoltage, -12.0, 12.0);
        motor.setControl(voltageOut.withOutput(appliedVoltage).withEnableFOC(true));
    }
}
