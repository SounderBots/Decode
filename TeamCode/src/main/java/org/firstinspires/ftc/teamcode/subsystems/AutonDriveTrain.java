package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import lombok.Getter;

@Getter
public class AutonDriveTrain extends DriveTrainBase {

    DcMotor odoX;
    DcMotor odoY;
    IMU imu;

    public AutonDriveTrain(HardwareMap hardwareMap, Telemetry telemetry) {
        super(hardwareMap, telemetry);

    }

    @Override
    protected void initHardware(HardwareMap hardwareMap) {
        super.initHardware(hardwareMap);

        odoX = hardwareMap.get(DcMotor.class, "odoX");
        odoY = hardwareMap.get(DcMotor.class, "odoY");
        imu = hardwareMap.get(IMU.class, "imu");
    }

    public void setWheelsPower(double frontLeftPower, double frontRightPower, double backLeftPower, double backRightPower) {
        frontLeft.motor.setPower(frontLeftPower);
        frontRight.motor.setPower(frontRightPower);
        backLeft.motor.setPower(backLeftPower);
        backRight.motor.setPower(backRightPower);
    }

    public void stop() {
        mecanumDrive.stop();
    }
}
