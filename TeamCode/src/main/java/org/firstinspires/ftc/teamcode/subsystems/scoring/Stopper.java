package org.firstinspires.ftc.teamcode.subsystems.scoring;

import android.util.Log;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;

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
