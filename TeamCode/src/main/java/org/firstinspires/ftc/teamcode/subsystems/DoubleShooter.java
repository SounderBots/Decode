package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DoubleShooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor leftFlywheel, rightFlywheel;

    Servo leftLauncher, rightLauncher;

    public DoubleShooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.rightFlywheel = new Motor(hardwareMap, "RightFlywheel");

        this.rightLauncher = hardwareMap.get(Servo.class,"RightLauncher");
    }

    @Override
    public void periodic() {
        super.periodic();

        double leftYValue = gamepad.getLeftY();

        if(Math.abs(leftYValue) > .1) {
            rightFlywheel.set(leftYValue);
        }
    }

    public void TurnShooterOn() {
        rightFlywheel.set(1);
    }

    public void TurnShooterOff() {
        rightFlywheel.set(0);
    }

    public void RightBallLaunch() {
        rightLauncher.setPosition(1);
    }

    public void RightBallStow() {
        rightLauncher.setPosition(.45);
    }

    public void RightBallReset() {
        rightLauncher.setPosition(0);
    }

}
