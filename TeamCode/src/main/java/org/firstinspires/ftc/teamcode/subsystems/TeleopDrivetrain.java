package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TeleopDrivetrain extends DriveTrainBase {

    GamepadEx gamepad;

    double direction = 1;

    public TeleopDrivetrain(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        super(hardwareMap, telemetry);
        this.gamepad = gamepad;
    }

    @Override
    public void periodic() {
        super.periodic();

        mecanumDrive.driveRobotCentric(
                -1 * gamepad.getLeftX() * direction,
                -1 * gamepad.getLeftY() * direction,
                gamepad.getRightX() * -1
        );
    }

    public void ToggleDirection() {
        direction = direction * -1;
    }
}
