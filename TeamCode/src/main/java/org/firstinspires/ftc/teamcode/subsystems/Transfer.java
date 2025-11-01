package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;

public class Transfer extends SubsystemBase {

    Telemetry telemetry;

    GamepadEx gamepad;

    Motor chamberMotor;

    protected DistanceSensor highArtifactSensor, frontArtifactSensor;

    Servo rightLauncher, leftLauncher, feeder;

    public Transfer(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.chamberMotor = new Motor(hardwareMap, "Intake");

        highArtifactSensor = hardwareMap.get(DistanceSensor.class, "HighSensor");
        frontArtifactSensor = hardwareMap.get(DistanceSensor.class, "FrontSensor");

        this.rightLauncher = hardwareMap.get(Servo.class,"RightLauncher");
        this.leftLauncher = hardwareMap.get(Servo.class,"LeftLauncher");

        this.feeder = hardwareMap.get(Servo.class,"Feeder");
    }

    @Override
    public void periodic() {
        super.periodic();

        boolean addTelemetry = true;
        if(addTelemetry) {
            telemetry.addData("high Sensor", GetArtifactSensorReading());
            telemetry.addData("is ball detected on high side", IsArtifactDetected());

            telemetry.addData("front Sensor", GetFrontSensorReading());
            telemetry.addData("is ball detected on front", IsFrontArtifactDetected());

            telemetry.addData("ball count", artifactCount);


            telemetry.update();
        }
    }

    public void TurnOnChamberRoller() {
        chamberMotor.set(MainTeleop.MainTeleopConfig.ChamberIntakePower);
    }

    public void TurnOnSlowChamberRoller() {
        chamberMotor.set(MainTeleop.MainTeleopConfig.ChamberIntakeSlowPower);
    }

    public void TurnOffChamberRoller() {
        chamberMotor.set(0);
    }

    private double GetArtifactSensorReading() {
        return this.highArtifactSensor.getDistance(DistanceUnit.MM);
    }

    public boolean IsArtifactDetected() {
        return this.GetArtifactSensorReading() < 80;
    }

    private double GetFrontSensorReading() {
        return this.frontArtifactSensor.getDistance(DistanceUnit.MM);
    }

    public boolean IsFrontArtifactDetected() {
        return this.GetFrontSensorReading() < 140;
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

    public void FeedArtifact() {
        feeder.setPosition(SingleShooter.ShooterConfig.FeederShoot);
    }

    public void ResetFeeder() {
        feeder.setPosition(0);
    }

    int artifactCount = 0;

    public void IncrementArtifactCount() {
        artifactCount++;
    }

    public void DecrementArtifactCount() {
        artifactCount--;
    }

    public void ResetArtifactCount() {
        artifactCount = 0;
    }

    public int GetArtifactCount() {
        return artifactCount;
    }

    boolean tryingToLoadArtifactIntoShooter = false;

    public void TryToLoadArtifactIntoShooter() {
        tryingToLoadArtifactIntoShooter = true;
    }

    public void NotTryingToLoadArtifactIntoShooter() {
        tryingToLoadArtifactIntoShooter = false;
    }

    public boolean IsAlreadyTryingToLoadArtifactIntoShooter() {
        return tryingToLoadArtifactIntoShooter;
    }
}
