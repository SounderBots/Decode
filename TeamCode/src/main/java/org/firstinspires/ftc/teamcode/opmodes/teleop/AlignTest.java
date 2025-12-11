package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.command.AutoAlignToShoot;
import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;

@TeleOp
public class AlignTest extends OpModeTemplate {

    @Override
    public void initialize() {
        super.initialize();

        LimeLightAlign limelight = new LimeLightAlign(hardwareMap, telemetry);
        TeleopDrivetrain drivetrain = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        RGBLightIndicator rgbLightIndicator = new RGBLightIndicator(hardwareMap, telemetry, "RGBIndicator");
        Shooter shooter = new Shooter(hardwareMap, driverGamepad, telemetry, rgbLightIndicator, limelight, "aligntest", true);

        driverGamepad.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(drivetrain::AutoAlignOn, drivetrain),
                                new AutoAlignToShoot(limelight, drivetrain, telemetry, 0, 3, false)
                        ));

        driverGamepad.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(drivetrain::AutoAlignOff, drivetrain)
                        ));

        register(limelight, drivetrain, rgbLightIndicator, shooter);
    }
}
