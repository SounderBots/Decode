package org.firstinspires.ftc.teamcode.subsystems.drivetrain;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import lombok.Getter;

public class DriveTrainBase extends SubsystemBase {

    protected Telemetry telemetry;

    @Getter
    MotorEx backRight;

    @Getter
    MotorEx backLeft;

    @Getter
    MotorEx frontRight;

    @Getter
    MotorEx frontLeft;

    @Getter
    MecanumDrive mecanumDrive;

    public DriveTrainBase(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        initHardware(hardwareMap);
    }

    protected void initHardware(HardwareMap hardwareMap) {
        this.backLeft = new MotorEx(hardwareMap, "BL", Motor.GoBILDA.RPM_435);
        this.backRight = new MotorEx(hardwareMap, "BR", Motor.GoBILDA.RPM_435);
        this.frontLeft = new MotorEx(hardwareMap, "FL", Motor.GoBILDA.RPM_435);
        this.frontRight = new MotorEx(hardwareMap, "FR", Motor.GoBILDA.RPM_435);

        this.backLeft.setZeroPowerBehavior( Motor.ZeroPowerBehavior.BRAKE);
        this.backRight.setZeroPowerBehavior( Motor.ZeroPowerBehavior.BRAKE);
        this.frontLeft.setZeroPowerBehavior( Motor.ZeroPowerBehavior.BRAKE);
        this.frontRight.setZeroPowerBehavior( Motor.ZeroPowerBehavior.BRAKE);


        this.mecanumDrive = new MecanumDrive(frontLeft, frontRight, backLeft, backRight);
        resetMotor(backLeft);
        resetMotor(backRight);
        resetMotor(frontLeft);
        resetMotor(frontRight);

    }

    private boolean isBrakeMode = true;

    public void ToggleBrakeCoast() {
        if(isBrakeMode) {
            SetCoastMode();
        } else {
            SetBrakeMode();
        }
        isBrakeMode = !isBrakeMode;
    }

    public void SetBrakeMode() {
        backLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        frontLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
    }

    public void SetCoastMode() {
        backLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
        backRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
        frontLeft.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
        frontRight.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
    }

    void resetMotor(Motor motor) {
        motor.stopAndResetEncoder();
        motor.resetEncoder();
    }

    public void Turn(double speed) {
        mecanumDrive.driveRobotCentric(0, 0, speed);
    }

    public void Stop() {
        mecanumDrive.stop();
    }

}
