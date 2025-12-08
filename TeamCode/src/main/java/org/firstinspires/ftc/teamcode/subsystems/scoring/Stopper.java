package org.firstinspires.ftc.teamcode.subsystems.scoring;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Stopper extends SubsystemBase {

    Telemetry telemetry;

    GamepadEx gamepad;

    Servo stopper;

    public Stopper(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.stopper = hardwareMap.get(Servo.class,"Stopper");
    }

    @Override
    public void periodic() {
        super.periodic();

    }
    public void Stop() {
        stopper.setPosition(0.875);
    }

    public void Go() {
        stopper.setPosition(0.5);
    }
}
