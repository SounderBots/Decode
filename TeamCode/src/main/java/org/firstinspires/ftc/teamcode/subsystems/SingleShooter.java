package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class SingleShooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor leftFlywheel, rightFlywheel;

    @Config
    public static class ShooterConfig {

        public static double ShooterPower = 0.63;
        public static double RightLauncherStow = 0.34;
        public static double LeftLauncherStow = 0.53;

        public static double FeederShoot = .3;

    }

    public SingleShooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.rightFlywheel = new Motor(hardwareMap, "RightFlywheel");
        this.leftFlywheel = new Motor(hardwareMap, "LeftFlywheel");
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

    boolean toggleShooter = false;
    public void ToggleShooter() {
        if(toggleShooter) {
            rightFlywheel.set(ShooterConfig.ShooterPower);
            leftFlywheel.set(-1 * ShooterConfig.ShooterPower);
        } else {
            rightFlywheel.set(0);
            leftFlywheel.set(0);
        }

        toggleShooter = !toggleShooter;
    }

//    public void TurnShooterOff() {
//        rightFlywheel.set(0);
//        leftFlywheel.set(0);
//    }

}
