package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class SingleShooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor leftFlywheel, rightFlywheel;

    Servo rightLauncher;

    public SingleShooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
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
            leftFlywheel.set(-1 * leftYValue);
        }
    }

    public void TurnShooterOn() {
        rightFlywheel.set(1);
        leftFlywheel.set(-1);
    }

    public void TurnShooterOff() {
        rightFlywheel.set(0);
        leftFlywheel.set(0);
    }

    public void BallLaunch() {
        rightLauncher.setPosition(1);
    }

    public void BallStow() {
        rightLauncher.setPosition(.45);
    }

    public void BallReset() {
        rightLauncher.setPosition(0);
    }
}
