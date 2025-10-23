package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Log;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import lombok.Getter;

@Getter
public class AutonDriveTrain extends DriveTrainBase {

    private static final String LOG_TAG = AutonDriveTrain.class.getSimpleName();

    GoBildaPinpointDriver odo;

    public AutonDriveTrain(HardwareMap hardwareMap, Telemetry telemetry) {
        super(hardwareMap, telemetry);

    }

    @Override
    protected void initHardware(HardwareMap hardwareMap) {
        super.initHardware(hardwareMap);

        this.odo = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
        this.odo.resetPosAndIMU();
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.REVERSED);
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

