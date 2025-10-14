package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.drivebase.RobotDrive;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import lombok.Getter;

public class DriveTrainBase extends SubsystemBase {

    protected Telemetry telemetry;

    @Getter
    Motor backRight;

    @Getter
    Motor backLeft;

    @Getter
    Motor frontRight;

    @Getter
    Motor frontLeft;

    @Getter
    MecanumDrive mecanumDrive;

    public DriveTrainBase(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        initHardware(hardwareMap);
    }

    protected void initHardware(HardwareMap hardwareMap) {
        this.backLeft = new Motor(hardwareMap, "BL");
        this.backRight = new Motor(hardwareMap, "BR");
        this.frontLeft = new Motor(hardwareMap, "FL");
        this.frontRight = new Motor(hardwareMap, "FR");

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
