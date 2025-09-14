package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Shooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor s1, s2;

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.s1 = new Motor(hardwareMap, "S1");
        this.s2 = new Motor(hardwareMap, "S2");

    }

    @Override
    public void periodic() {
        super.periodic();

        s1.set(gamepad.getLeftY());
        s2.set(gamepad.getLeftY());
    }
}
