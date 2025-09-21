package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class AutonDriveTrain extends DriveTrainBase {

    GoBildaPinpointDriver odo;

    public AutonDriveTrain(HardwareMap hardwareMap, Telemetry telemetry) {
        super(hardwareMap, telemetry);

    }

    @Override
    protected void initHardware(HardwareMap hardwareMap) {
        super.initHardware(hardwareMap);

        // Make sure your robot configuration file has the Pinpoint sensor configured as an I2C device with name "odo" and I2C address (typically 0x31 for GoBilda Pinpoint).
        this.odo = new GoBildaPinpointDriver(hardwareMap.get(I2cDeviceSynch.class, "odo"), true);
        this.odo.resetPosAndIMU();
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.REVERSED);
    }
}
