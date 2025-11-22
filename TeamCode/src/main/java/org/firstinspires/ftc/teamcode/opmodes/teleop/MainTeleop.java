package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;

@TeleOp
public class MainTeleop extends OpModeTemplate {

    TeleopDrivetrain drive;
    Intake intake;

    Shooter shooter;

    LimeLightAlign limeLight;

    TransferChamber transfer;

    RGBLightIndicator light;

    @Config
    public static class Telemetry {
        public static boolean Shooter = false;

        public static boolean LimeLight = false;
    }


    @Config
    public static class MainTeleopConfig {
        public static long TransferDelay = 200;

        public static double ChamberIntakePower = -0.9;

        public static double ChamberIntakeSlowPower = -0.3;
    }

    @Override
    public void initialize() {
        super.initialize();

        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        this.light = new RGBLightIndicator(hardwareMap, telemetry, "RGBIndicator");
        this.intake = new Intake(hardwareMap, operatorGamepad, telemetry);
        this.shooter = new Shooter(hardwareMap, operatorGamepad, telemetry, light);
        this.transfer = new TransferChamber(hardwareMap, operatorGamepad, telemetry);
        this.limeLight = new LimeLightAlign(hardwareMap, telemetry);

        new Trigger(() -> gamepad2.right_stick_y < -0.5)
                .whenActive(new InstantCommand(intake::StartIntake, intake));

        new Trigger(() -> gamepad2.right_stick_y < 0.5)
                .whenActive(new InstantCommand(intake::StopIntake, intake));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.LEFT_BUMPER)
                .whenPressed(new InstantCommand(intake::ToggleOuttake, intake));

        new Trigger(() -> gamepad2.left_trigger > 0.5)
                .whenActive(new InstantCommand(shooter::FarShoot, shooter));

        new Trigger(() -> gamepad2.right_trigger > 0.5)
                .whenActive(new InstantCommand(shooter::CloseShoot, shooter));


        operatorGamepad.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::TopRollersOuttake, transfer),
                                new InstantCommand(transfer::TurnOnSlowChamberRoller, transfer)
                        ))
                .whenReleased(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::TopRollersStop, transfer),
                                new InstantCommand(transfer::TurnOffChamberRoller, transfer)
                        )
                );

        operatorGamepad.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::TopRollersIntake, transfer),
                                new InstantCommand(transfer::TurnOnSlowChamberRollerInReverse, transfer)
                        ))
                .whenReleased(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::TopRollersStop, transfer),
                                new InstantCommand(transfer::TurnOffChamberRoller, transfer)
                        )
                );

        operatorGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(transfer::FeedArtifact, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new InstantCommand(transfer::ResetFeeder, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::FeedArtifact, transfer),
                                new WaitCommand(MainTeleopConfig.TransferDelay),
//                                new InstantCommand(transfer::BallStow, transfer),
                                new InstantCommand(transfer::ResetFeeder, transfer)
                        )
                );

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_STICK_BUTTON)
                .whenPressed(new InstantCommand(drive::ToggleDirection, drive));

        driverGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(intake::StopIntake, intake));

        register(drive, intake, shooter, light, limeLight);
    }
}
