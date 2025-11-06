package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;

@TeleOp
public class LimeLightSubSystemTest extends OpModeTemplate {

    LimeLightAlign limeLight;

    @Override
    public void initialize() {
        super.initialize();
        this.limeLight = new LimeLightAlign(hardwareMap, telemetry);


        register(limeLight);
    }
}
