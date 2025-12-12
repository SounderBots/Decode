package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;

//@TeleOp
public class ShooterTest extends OpModeTemplate {

    Shooter shooter;

    @Config
    public static class MainTeleopConfig {
        public static long TransferDelay = 200;

        public static double ChamberIntakePower = -0.5;

        public static double ChamberIntakeSlowPower = -0.3;
    }

    @Override
    public void initialize() {
        super.initialize();

        RGBLightIndicator light = new RGBLightIndicator(hardwareMap, telemetry, "light");

        this.shooter = new Shooter(hardwareMap, driverGamepad, telemetry, light);

        driverGamepad.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(shooter::CloseShoot, shooter));

        register(light);
    }
}
