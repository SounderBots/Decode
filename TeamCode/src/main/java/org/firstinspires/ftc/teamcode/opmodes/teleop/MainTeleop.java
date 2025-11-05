package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.feedback.DriverFeedback;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.scoring.SingleShooter;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;

@TeleOp
public class MainTeleop extends OpModeTemplate {

    TeleopDrivetrain drive;
    Intake intake;

    SingleShooter shooter;

    LimeLightSubsystem limeLight;

    TransferChamber transfer;

    DriverFeedback feedback;

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

        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        this.intake = new Intake(hardwareMap, operatorGamepad, telemetry);
        this.shooter = new SingleShooter(hardwareMap, operatorGamepad, telemetry);
        this.transfer = new TransferChamber(hardwareMap, operatorGamepad, telemetry);
        //this.limeLight = new LimeLightSubsystem(hardwareMap, telemetry);
        this.feedback = new DriverFeedback(hardwareMap, driverGamepad, operatorGamepad, telemetry);
        this.light = new RGBLightIndicator(hardwareMap, telemetry, "RGBIndicator");

        new Trigger(() -> gamepad2.right_stick_y < -0.5)
                .whenActive(new InstantCommand(intake::StartIntake, intake));

        new Trigger(() -> gamepad2.right_stick_y < 0.5)
                .whenActive(new InstantCommand(intake::StopIntake, intake));

        new Trigger(() -> gamepad2.left_trigger > 0.5)
                .whenActive(new InstantCommand(shooter::FarShoot, shooter));

        new Trigger(() -> gamepad2.right_trigger > 0.5)
                .whenActive(new InstantCommand(shooter::CloseShoot, shooter));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.Y)
                .whenPressed(new InstantCommand(transfer::BallStow, shooter));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::BallLaunch, transfer),
                                new WaitCommand(200),
                                new InstantCommand(transfer::BallReset, transfer)
                        ));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.X)
                .whenPressed(new InstantCommand(transfer::BallReset, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(transfer::ResetFeeder, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(transfer::FeedArtifact, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new InstantCommand(transfer::ResetFeeder, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::FeedArtifact, transfer),
                                new WaitCommand(MainTeleopConfig.TransferDelay),
                                new InstantCommand(transfer::BallStow, transfer),
                                new InstantCommand(transfer::ResetFeeder, transfer)
                        )
                );

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(light::changeGreen, light));

        driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new InstantCommand(light::changeGreen, light));

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_STICK_BUTTON)
                .whenPressed(new InstantCommand(drive::ToggleDirection, drive));

        register(drive, intake, shooter, feedback, light);

//        schedule(
//                new AutoLoadShooterCommand(telemetry, transfer),
//                new AutoIntakeCommand(telemetry, transfer)
//                );
    }
}
