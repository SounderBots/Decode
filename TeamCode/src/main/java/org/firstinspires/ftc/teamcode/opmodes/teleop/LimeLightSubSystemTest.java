package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.LimeLightSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TeleopDrivetrain;

@TeleOp
public class LimeLightSubSystemTest extends OpModeTemplate {


    TeleopDrivetrain drive;
    LimeLightSubsystem limeLight;

    @Override
    public void initialize() {
        super.initialize();
        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        this.limeLight = new LimeLightSubsystem(hardwareMap, telemetry);


        register(drive, limeLight);
    }
}
