package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Shooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    MotorEx s1, s2;
    Servo servoLeft, servoRight;

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.s1 = new MotorEx(hardwareMap, "S1", Motor.GoBILDA.BARE);
        this.s2 = new MotorEx(hardwareMap, "S2", Motor.GoBILDA.BARE);
    }

    @Override
    public void periodic() {
        super.periodic();
        if (gamepad.getLeftY()>0) {
            s1.set(1);
            s2.set(-1);
        } else {
            s1.set(0);
            s2.set(0);
        }
    }
}
