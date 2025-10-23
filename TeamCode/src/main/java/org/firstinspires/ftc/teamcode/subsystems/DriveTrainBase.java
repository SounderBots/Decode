package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.drivebase.RobotDrive;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
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

        this.mecanumDrive = new MecanumDrive(frontLeft, frontRight, backLeft, backRight);
        resetMotor(backLeft);
        resetMotor(backRight);
        resetMotor(frontLeft);
        resetMotor(frontRight);

    }

    void resetMotor(Motor motor) {
        motor.stopAndResetEncoder();
        motor.resetEncoder();
    }

}
