package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TeleopDrivetrain extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;

    Motor backRight;
    Motor backLeft;
    Motor frontRight;
    Motor frontLeft;

    MecanumDrive mecanumDrive;

    double direction = 1;

    public TeleopDrivetrain(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.backLeft = new Motor(hardwareMap, "BL");
        this.backRight = new Motor(hardwareMap, "BR");
        this.frontLeft = new Motor(hardwareMap, "FL");
        this.frontRight = new Motor(hardwareMap, "FR");

        this.mecanumDrive = new MecanumDrive(frontLeft, frontRight, backLeft, backRight);
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
