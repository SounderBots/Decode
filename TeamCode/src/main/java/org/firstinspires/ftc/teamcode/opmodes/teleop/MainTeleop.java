package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.command.AutoIntakeCommand;
import org.firstinspires.ftc.teamcode.command.AutoLoadShooterCommand;
import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.SingleShooter;
import org.firstinspires.ftc.teamcode.subsystems.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Transfer;

@TeleOp
public class MainTeleop extends OpModeTemplate {

    TeleopDrivetrain drive;
    Intake intake;

    SingleShooter shooter;

    Transfer transfer;
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
        this.transfer = new Transfer(hardwareMap, operatorGamepad, telemetry);

        operatorGamepad.getGamepadButton(GamepadKeys.Button.Y)
                .whenPressed(new InstantCommand(shooter::ToggleShooter, shooter));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(transfer::BallLaunch, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.X)
                .whenPressed(new InstantCommand(transfer::BallReset, transfer));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(transfer::BallStow, transfer));

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

        register(drive, intake, shooter);
        schedule(
                new AutoLoadShooterCommand(telemetry, transfer),
                new AutoIntakeCommand(telemetry, transfer)
                );
    }
}
