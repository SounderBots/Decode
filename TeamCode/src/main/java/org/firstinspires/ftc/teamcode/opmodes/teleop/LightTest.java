package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;

//@TeleOp
public class LightTest extends OpModeTemplate {

    RGBLightIndicator light;

    @Config
    public static class MainTeleopConfig {
        public static long TransferDelay = 200;

        public static double ChamberIntakePower = -0.5;

        public static double ChamberIntakeSlowPower = -0.3;
    }

    @Override
    public void initialize() {
        super.initialize();

        this.light = new RGBLightIndicator(hardwareMap, telemetry, "RGBIndicator");

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(light::changeGreen, light));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new InstantCommand(light::changeRed, light));



        register(light);
    }
}
