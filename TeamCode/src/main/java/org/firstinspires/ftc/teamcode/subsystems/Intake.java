package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Intake extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    Motor motor;

    protected DistanceSensor artifactSensor;

    public Intake(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.motor = new Motor(hardwareMap, "Intake");
        artifactSensor = hardwareMap.get(DistanceSensor.class, "BallSensor");
    }

    @Override
    public void periodic() {
        super.periodic();

        motor.set(gamepad.getRightY());

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
}
