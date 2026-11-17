package org.team7525.swerve;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Fahrenheit;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import org.team7525.swerve.ModuleInputRecords.DriveData;
import org.team7525.swerve.ModuleInputRecords.ModuleData;
import org.team7525.swerve.ModuleInputRecords.TurnData;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Temperature;
import org.wpilib.units.measure.Voltage;

public class Module {
    private final TalonFX drive;
    private final TalonFX steer;
    private final CANcoder encoder;

    private final StatusSignal<Angle> drivePosition;
    private final StatusSignal<AngularVelocity> driveVelocity;
    private final StatusSignal<Voltage> driveAppliedVolts;
    private final StatusSignal<Current> driveTorqueCurrent;
    private final StatusSignal<Current> driveSupplyCurrent;
    private final StatusSignal<Temperature> driveTemp;
    private final StatusSignal<Angle> steerPosition;
    private final StatusSignal<AngularVelocity> steerVelocity;
    private final StatusSignal<Voltage> steerAppliedVolts;
    private final StatusSignal<Current> steerTorqueCurrent;
    private final StatusSignal<Current> steerSupplyCurrent;
    private final StatusSignal<Temperature> steerTemp;
    private final StatusSignal<Angle> encoderPosition;

    public Module(TalonFX drive, TalonFX steer, CANcoder encoder) {
        this.drive = drive;
        this.steer = steer;
        this.encoder = encoder;

        this.drivePosition = drive.getPosition();
        this.driveVelocity = drive.getVelocity();
        this.driveAppliedVolts = drive.getMotorVoltage();
        this.driveTorqueCurrent = drive.getTorqueCurrent();
        this.driveSupplyCurrent = drive.getSupplyCurrent();
        this.driveTemp = drive.getDeviceTemp();

        this.steerPosition = steer.getPosition();
        this.steerVelocity = steer.getVelocity();
        this.steerAppliedVolts = steer.getMotorVoltage();
        this.steerTorqueCurrent = steer.getTorqueCurrent();
        this.steerSupplyCurrent = steer.getSupplyCurrent();
        this.steerTemp = steer.getDeviceTemp();

        this.encoderPosition = encoder.getPosition();
    }

    public DriveData getDriveData() {
        return new DriveData(
                drive.isConnected(),
                drivePosition.getValue().in(Radians),
                driveVelocity.getValue().in(RadiansPerSecond),
                driveAppliedVolts.getValue().in(Volts),
                driveSupplyCurrent.getValue().in(Amps),
                driveTorqueCurrent.getValue().in(Amps),
                driveTemp.getValue().in(Fahrenheit));
    }

    public TurnData getSteerData() {
        return new TurnData(
                steer.isConnected(),
                encoder.isConnected(),
                Rotation2d.fromRadians(encoderPosition.getValue().in(Radians)),
                Rotation2d.fromRadians(steerPosition.getValue().in(Radians)),
                steerVelocity.getValue().in(RadiansPerSecond),
                steerAppliedVolts.getValue().in(Volts),
                steerSupplyCurrent.getValue().in(Amps),
                steerTorqueCurrent.getValue().in(Amps),
                steerTemp.getValue().in(Fahrenheit));
    }

    public ModuleData getModuleData() {
        return new ModuleData(getDriveData(), getSteerData());
    }
}
