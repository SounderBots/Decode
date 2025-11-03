package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightSubsystem;

@TeleOp
public class LimeLightSubSystemTest extends OpModeTemplate {

    LimeLightSubsystem limeLight;

    @Override
    public void initialize() {
        super.initialize();
        this.limeLight = new LimeLightSubsystem(hardwareMap, telemetry);


        register(limeLight);
    }
}
