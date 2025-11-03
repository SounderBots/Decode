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

        this.motor = new Motor(hardwareMap, "FrontIntake");
    }

    @Override
    public void periodic() {
        super.periodic();

//        if(Math.abs(gamepad.getLeftY()) > .2) {
//            motor.set(gamepad.getLeftY() * SingleShooter.ShooterConfig.IntakeMaxPower);
//        } else {
//            motor.set(0);
//
//            if(Math.abs(gamepad.getRightY()) > .2) {
//                motor.set(gamepad.getRightY() * SingleShooter.ShooterConfig.IntakeMaxPower);
//            } else {
//                motor.set(0);
//
//            }
//        }
    }

    public void StartIntake() {
        motor.set(SingleShooter.ShooterConfig.IntakeMaxPower);
    }

    public void StopIntake() {
        motor.set(0);
    }
}
