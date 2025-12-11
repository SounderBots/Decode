package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.feedback.DriverFeedback;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Stopper;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;

@TeleOp
@Configurable
public class MainTeleop extends OpModeTemplate {

    TeleopDrivetrain drive;
    Intake intake;

    Shooter shooter;

    LimeLightAlign limeLight;

    TransferChamber transfer;

    Stopper stopper;

    RGBLightIndicator light;

    DriverFeedback feedback;

    @Config
    public static class Telemetry {
        public static boolean Shooter = false;

        public static boolean LimeLight = false;
    }

    @Configurable
    @Config
    public static class MainTeleopConfig {
        public static long TransferDelay = 200;

        public static double ChamberIntakePower = -0.9;

        public static double ChamberIntakeSlowPower = -0.8;
    }

    @Override
    public void initialize() {
        super.initialize();

        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        this.light = new RGBLightIndicator(hardwareMap, telemetry, "RGBIndicator");
        this.intake = new Intake(hardwareMap, operatorGamepad, telemetry);
        this.limeLight = new LimeLightAlign(hardwareMap, telemetry);
        this.shooter = new Shooter(hardwareMap, operatorGamepad, telemetry, light, limeLight, "MainTeleop");
        this.transfer = new TransferChamber(hardwareMap, operatorGamepad, telemetry);
        this.feedback = new DriverFeedback(hardwareMap, driverGamepad, operatorGamepad, telemetry);
        this.stopper = new Stopper(hardwareMap, operatorGamepad, telemetry);

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
                                new InstantCommand(() -> {
                                    shooter.SetShootingFlag();
                                    transfer.TurnOnChamberRoller();
                                }, transfer),
                                new InstantCommand(stopper::Go, stopper)
                        ))
                .whenReleased(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::TurnOffChamberRoller, transfer),
                                new InstantCommand(stopper::Stop, stopper)
                        )
                );

        operatorGamepad.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(

                new SequentialCommandGroup(
                        new InstantCommand(transfer::TurnOnSlowChamberRoller, transfer),
                        new InstantCommand(stopper::Go, stopper)

                ))
                .whenReleased(
                        new SequentialCommandGroup(
                                new InstantCommand(transfer::TurnOffChamberRoller, transfer),
                                new InstantCommand(stopper::Stop, stopper)
                        )
                );

        operatorGamepad.getGamepadButton(GamepadKeys.Button.DPAD_UP)
                .whenPressed(

                                new InstantCommand(shooter::HigherTilt, shooter)
                );

        operatorGamepad.getGamepadButton(GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(

                        new InstantCommand(shooter::LowerTilt, shooter)
                );

        // Turn on auto shooter rpm and tilt
        operatorGamepad.getGamepadButton(GamepadKeys.Button.X)
                .whenPressed(
                        new SequentialCommandGroup(
                                new InstantCommand(shooter::AutoSpeedAndTilt, shooter),
                                new InstantCommand(feedback::OperatorRumbleBlip, feedback)
                        )
                );

        // Turn on auto shooter rpm and tilt
        operatorGamepad.getGamepadButton(GamepadKeys.Button.Y)
                .whenPressed(
                        new InstantCommand(shooter::DefaultSpeedAndTilt, shooter)
                );

        driverGamepad.getGamepadButton(GamepadKeys.Button.LEFT_STICK_BUTTON)
                .whenPressed(new InstantCommand(drive::ToggleDirection, drive));

        driverGamepad.getGamepadButton(GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new InstantCommand(intake::StopIntake, intake));

        register(drive, intake, shooter, light, limeLight, stopper);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        try {
            super.runOpMode();
        } catch (InterruptedException e) {
            //do nothing
        } finally {
            if (shooter != null) {
                shooter.stopLogging();
            }
        }
    }
}
