package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Turret;

@TeleOp
public class TurretTest extends OpModeTemplate {

  Turret turret;

  @Override
  public void initialize() {
    super.initialize();

    this.turret = new Turret(hardwareMap, driverGamepad, telemetry, null);

    driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_LEFT)
        .whenPressed(new InstantCommand(turret::TurnLeft, turret))
        .whenReleased(new InstantCommand(turret::StopTurret, turret));

    driverGamepad.getGamepadButton(GamepadKeys.Button.DPAD_RIGHT)
        .whenPressed(new InstantCommand(turret::TurnRight, turret))
        .whenReleased(new InstantCommand(turret::StopTurret, turret));

    driverGamepad.getGamepadButton(GamepadKeys.Button.A)
        .whenPressed(new InstantCommand(turret::CloseShoot, turret));

    driverGamepad.getGamepadButton(GamepadKeys.Button.X)
        .whenPressed(new InstantCommand(turret::FarShoot, turret));

    register(turret);
  }
}
