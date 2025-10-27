package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Intake extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor motor;
    Servo IntakeKickerServo1;
    Servo IntakeKickerServo2;

    public Intake(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.motor = new Motor(hardwareMap, "Intake");
        this.IntakeKickerServo1 = hardwareMap.get(Servo.class,"IS1");
        this.IntakeKickerServo2 = hardwareMap.get(Servo.class,"IS2");
    }

    @Override
    public void periodic() {
        super.periodic();

        motor.set(gamepad.getRightY());
    }
}
