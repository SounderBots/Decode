package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.DoubleShooter;
import org.firstinspires.ftc.teamcode.subsystems.SingleShooter;
import org.firstinspires.ftc.teamcode.subsystems.TeleopDrivetrain;

@TeleOp
public class MainTeleop extends OpModeTemplate {

    TeleopDrivetrain drive;
    Intake intake;

    SingleShooter shooter;

    @Override
    public void initialize() {
        super.initialize();

        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        this.intake = new Intake(hardwareMap, operatorGamepad, telemetry);
        this.shooter = new SingleShooter(hardwareMap, operatorGamepad, telemetry);

        operatorGamepad.getGamepadButton(GamepadKeys.Button.Y)
                .whenPressed(new InstantCommand(shooter::TurnShooterOn, shooter));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.A)
                .whenPressed(new InstantCommand(shooter::BallLaunch, shooter));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.X)
                .whenPressed(new InstantCommand(shooter::BallReset, shooter));

        operatorGamepad.getGamepadButton(GamepadKeys.Button.B)
                .whenPressed(new InstantCommand(shooter::BallStow, shooter));

        register(drive, intake, shooter);
    }
}
