package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import lombok.Getter;

@Getter
public class AutonDriveTrain extends DriveTrainBase {

    private static final String LOG_TAG = AutonDriveTrain.class.getSimpleName();

    GoBildaPinpointDriver pinpoint;

    public AutonDriveTrain(HardwareMap hardwareMap, Telemetry telemetry) {
        super(hardwareMap, telemetry);

    }

    @Override
    protected void initHardware(HardwareMap hardwareMap) {
        super.initHardware(hardwareMap);

        this.pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
        this.pinpoint.resetPosAndIMU();
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.REVERSED);
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

