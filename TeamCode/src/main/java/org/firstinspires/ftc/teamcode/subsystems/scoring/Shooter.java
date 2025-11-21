package org.firstinspires.ftc.teamcode.subsystems.scoring;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.arcrobotics.ftclib.controller.wpilibcontroller.SimpleMotorFeedforward;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;

public class Shooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    MotorEx leftFlywheel, rightFlywheel;

    Servo liftServo;

    RGBLightIndicator speedIndicator;

    @Config
    public static class ShooterConfig {

        public static double ShooterRpmHi = 930;

        public static double ShooterRpmLo = 750;

        public static double RightLauncherStow = 0.34;

        public static double LeftLauncherStow = 0.53;

        public static double FeederShoot = .4;

        public static double FeederReset = .9;

        public static double IntakeMaxPower = 1;

        public static double TiltServoHi = .5;

        public static double TiltServoLo = 0;

        public static double FlywheelAcceptableRpmError = 40;
        }

    @Config
    public static class ShooterControlConfig {

        // PID

        public static double kP = 0.0095;

        public static double kI = 0.005;

        public static double kD = 0;

        // Feedforward

        public static double ks = 0;

        public static double kv_left = 0.00032;

        public static double kv_right = 0.00038;

        public static double ka = .01;
    }

    SimpleMotorFeedforward leftFeedforward = new SimpleMotorFeedforward(ShooterControlConfig.ks, ShooterControlConfig.kv_left, ShooterControlConfig.ka);
    SimpleMotorFeedforward rightFeedforward = new SimpleMotorFeedforward(ShooterControlConfig.ks, ShooterControlConfig.kv_right, ShooterControlConfig.ka);

    PIDFController leftPid = new PIDFController(ShooterControlConfig.kP, ShooterControlConfig.kI, ShooterControlConfig.kD, 0.0);
    PIDFController rightPid = new PIDFController(ShooterControlConfig.kP, ShooterControlConfig.kI, ShooterControlConfig.kD, 0.0);

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry, RGBLightIndicator speedIndicator) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.speedIndicator = speedIndicator;

        this.rightFlywheel = new MotorEx(hardwareMap, "RightFlywheel", Motor.GoBILDA.BARE);
        this.leftFlywheel = new MotorEx(hardwareMap, "LeftFlywheel", Motor.GoBILDA.BARE);

        this.liftServo = hardwareMap.get(Servo.class,"LiftServo");

        this.rightFlywheel.setRunMode(Motor.RunMode.RawPower);
        this.leftFlywheel.setRunMode(Motor.RunMode.RawPower);

        this.leftFlywheel.setInverted(true);

        this.rightFlywheel.setZeroPowerBehavior( Motor.ZeroPowerBehavior.FLOAT);
        this.leftFlywheel.setZeroPowerBehavior( Motor.ZeroPowerBehavior.FLOAT);

        speedIndicator.changeRed();

        if(MainTeleop.Telemetry.Shooter) {
            telemetry.addData("target", this.targetVelocity);
            telemetry.addData("right velocity", 0);
            telemetry.addData("right error", 0);
            telemetry.addData("right power (pid)", 0);
            telemetry.addData("right power (ff)", 0);
            telemetry.addData("current right power", 0);

            telemetry.addData("left velocity", 0);
            telemetry.addData("left error", 0);
            telemetry.addData("left power (pid)", 0);
            telemetry.addData("left power (ff)", 0);
            telemetry.addData("current left power", 0);

            telemetry.update();
        }
    }

    boolean wasLastColorGreen = false;

    @Override
    public void periodic() {
        super.periodic();

        double rightVelocity = rightFlywheel.getVelocity();
        double rightError = targetVelocity - rightVelocity;

        double leftVelocity = leftFlywheel.getVelocity();
        double leftError = targetVelocity - leftVelocity;

        if(Math.abs(rightError) < ShooterConfig.FlywheelAcceptableRpmError &&
                Math.abs(leftError) < ShooterConfig.FlywheelAcceptableRpmError &&
                Math.abs(leftVelocity - rightVelocity) < 30
        ){
            if(!wasLastColorGreen) {
                wasLastColorGreen = true;
                speedIndicator.changeGreen();
            }
        } else {
            if(wasLastColorGreen) {
                wasLastColorGreen = false;
                speedIndicator.changeRed();
            }
        }

        // --- Compute feedback + feedforward ---
        // PIDFController works in "measurement, setpoint" order
        double leftPidPower = leftPid.calculate(leftVelocity, targetVelocity);
        double rightPidPower = rightPid.calculate(rightVelocity, targetVelocity);


        // For a shooter, we usually assume accel ~ 0 in steady state
        double leftFeedforwardValue = leftFeedforward.calculate(targetVelocity);
        double rightFeedforwardValue = rightFeedforward.calculate(targetVelocity);

        // Total output to motors (you tune k's so this ends up in [-1, 1])
        double leftPower = leftFeedforwardValue + leftPidPower;
        double rightPower = rightFeedforwardValue + rightPidPower;

        leftPower = clamp(leftPower, -1.0, 1.0);
        rightPower = clamp(rightPower, -1.0, 1.0);

        rightFlywheel.set(rightPower);
        leftFlywheel.set(leftPower);

        if(MainTeleop.Telemetry.Shooter) {
            telemetry.addData("target", this.targetVelocity);

            telemetry.addData("right velocity", rightVelocity);
            telemetry.addData("right error", rightError);
            telemetry.addData("right power (pid)", rightPidPower);
            telemetry.addData("right power (ff)", rightFeedforwardValue);
            telemetry.addData("current right power", rightPower);

            telemetry.addData("left velocity", leftVelocity);
            telemetry.addData("left error", leftError);
            telemetry.addData("left power (pid)", leftPidPower);
            telemetry.addData("left power (ff)", leftFeedforwardValue);
            telemetry.addData("current left power", leftPower);

            telemetry.update();
        }
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    double targetVelocity = ShooterConfig.ShooterRpmHi;

    boolean toggleShooter = false;
    public void ToggleShooter() {
        if(toggleShooter) {
            this.targetVelocity = ShooterConfig.ShooterRpmLo;
        } else {
            this.targetVelocity = ShooterConfig.ShooterRpmHi;
        }

        toggleShooter = !toggleShooter;
    }

    public void SetTargetRpm(double targetRpm) {
        this.targetVelocity = targetRpm;
    }

    public void CloseShoot() {
        this.liftServo.setPosition(ShooterConfig.TiltServoHi);
        this.targetVelocity = ShooterConfig.ShooterRpmLo;
    }

    public void CloseShootWithScale(double scale) {
        this.liftServo.setPosition(ShooterConfig.TiltServoHi);
        this.targetVelocity = ShooterConfig.ShooterRpmLo * scale;
    }

    public void FarShoot() {
        this.liftServo.setPosition(ShooterConfig.TiltServoLo);
        this.targetVelocity = ShooterConfig.ShooterRpmHi;
    }

    public void FarShootWithScale(double scale) {
        this.liftServo.setPosition(ShooterConfig.TiltServoLo);
        this.targetVelocity = ShooterConfig.ShooterRpmHi * scale;
    }

    public boolean isReadyToShoot() {
        return wasLastColorGreen;
    }
}
