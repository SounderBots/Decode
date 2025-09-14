package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.TeleopDrivetrain;

@TeleOp
public class MecanumDrive extends MainTeleop {

    TeleopDrivetrain drive;

    @Override
    public void initialize() {
        super.initialize();

        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);

        register(drive);
    }
}
