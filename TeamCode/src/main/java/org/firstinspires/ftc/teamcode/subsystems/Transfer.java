package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Transfer extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;

    protected DistanceSensor artifactSensor;

    Servo rightLauncher, leftLauncher;


    public Transfer(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        artifactSensor = hardwareMap.get(DistanceSensor.class, "BallSensor");

        this.rightLauncher = hardwareMap.get(Servo.class,"RightLauncher");
        this.leftLauncher = hardwareMap.get(Servo.class,"LeftLauncher");
    }

    @Override
    public void periodic() {
        super.periodic();

        telemetry.addData("distance", GetArtifactSensorReading());
        telemetry.addData("is ball detected", IsArtifactDetected());
        telemetry.update();
    }

    private double GetArtifactSensorReading() {
        return this.artifactSensor.getDistance(DistanceUnit.MM);
    }

    private boolean IsArtifactDetected() {
        return this.GetArtifactSensorReading() < 70;
    }

    public void BallLaunch() {
        rightLauncher.setPosition(1);
        leftLauncher.setPosition(0);
    }

    public void BallStow() {
        rightLauncher.setPosition(SingleShooter.ShooterConfig.RightLauncherStow);
        leftLauncher.setPosition(SingleShooter.ShooterConfig.LeftLauncherStow);

    }

    public void BallReset() {
        rightLauncher.setPosition(0);
        leftLauncher.setPosition(1);
    }
}
