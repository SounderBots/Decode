package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Intake extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor motor;

    public Intake(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.motor = new Motor(hardwareMap, "Intake");
    }

    @Override
    public void periodic() {
        super.periodic();

        motor.set(gamepad.getRightY());
    }
}
