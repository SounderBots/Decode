package org.firstinspires.ftc.teamcode.subsystems.drivetrain;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TeleopDrivetrain extends DriveTrainBase {

    final double power = 1d;
    GamepadEx gamepad;

    double direction = 1;

    public TeleopDrivetrain(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        super(hardwareMap, telemetry);
        this.gamepad = gamepad;
    }

    @Override
    public void periodic() {
        super.periodic();

        // A bit tricky here. If autoalign is turned on, then we turn off this teleop drive
        // since it counteracts the power sent for alignment (causes jerky motion).
        // We revert back to teleop behavior after the command complete - either
        // aligns or times out after 1500ms. The other way to revert is for the Driver
        // to hit the 'B' button
        if(!autoAlign) {
            mecanumDrive.driveRobotCentric(
                    gamepad.getLeftX() * power * -1 * direction,
                    gamepad.getLeftY() * power * -1 * direction,
                    gamepad.getRightX() * power * -.7
            );
        }
    }

    public void ToggleDirection() {
        direction = direction * -1;
    }

    boolean autoAlign = false;

    public void AutoAlignOn() {
        this.autoAlign = true;
    }

    public void AutoAlignOff() {
        this.autoAlign = false;
    }
}
