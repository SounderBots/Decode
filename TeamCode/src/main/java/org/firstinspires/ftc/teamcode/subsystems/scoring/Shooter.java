package org.firstinspires.ftc.teamcode.subsystems.scoring;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.util.SonicPIDFController;

public class Shooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    MotorEx leftFlywheel, rightFlywheel;

    SonicPIDFController rightShooterPid = new SonicPIDFController(ShooterConfig.kP, 0, 0);
    SonicPIDFController leftShooterPid = new SonicPIDFController(ShooterConfig.kP, 0, 0);

    Servo liftServo;

    RGBLightIndicator speedIndicator;

    @Config
    public static class ShooterConfig {

        public static double ShooterRpmHi = 1550;

        public static double ShooterRpmLo = 1375;

        public static double RightLauncherStow = 0.34;

        public static double LeftLauncherStow = 0.53;

        public static double FeederShoot = .4;

        public static double FeederReset = .9;

        public static double IntakeMaxPower = 1;

        public static double kP = 0.00001;

        public static double kI = 0;

        public static double kD = 0.00005;


        public static double TiltServoHi = .6;

        public static double TiltServoLo = 0;

        public static double FlywheelAcceptableRpmError = 40;
        }

    @Config
    public static class ShooterFeedforwardConfig {
        public static double FeedforwardBoost = .05;

        public static double FeedforwardBoostStartMs = 200;

        public static double FeedforwardBoostEndMs = 400;

        public static double FeedForwardDampner = .5;

    }

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

        rightFlywheel.setVeloCoefficients(ShooterConfig.kP, ShooterConfig.kI, ShooterConfig.kD);
        leftFlywheel.setVeloCoefficients(ShooterConfig.kP, ShooterConfig.kI, ShooterConfig.kD);

        speedIndicator.changeRed();
    }

    double currentLeftPower = 0.63;
    double currentRightPower = 0.63;

    boolean wasLastColorGreen = false;

    double currentBoost = 0;

    @Override
    public void periodic() {
        super.periodic();

        double rightVelocity = rightFlywheel.getVelocity();
        double rightError = targetVelocity - rightVelocity;
        double rightPowerDelta = rightShooterPid.calculatePIDAlgorithm(rightError);

        double leftVelocity = leftFlywheel.getVelocity();
        double leftError = targetVelocity - leftVelocity;
        double leftPowerDelta = leftShooterPid.calculatePIDAlgorithm(leftError);

        if(Math.abs(rightError) < ShooterConfig.FlywheelAcceptableRpmError && Math.abs(leftError) < ShooterConfig.FlywheelAcceptableRpmError){
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

        currentRightPower += rightPowerDelta;
        currentLeftPower += leftPowerDelta;

        double elaspedTime = feedforwardTimer.milliseconds();
//        if(elaspedTime > ShooterFeedforwardConfig.FeedforwardBoostStartMs && elaspedTime < ShooterFeedforwardConfig.FeedforwardBoostEndMs) {
//            currentRightPower += currentBoost;
//            currentLeftPower += currentBoost;
//            currentBoost *= ShooterFeedforwardConfig.FeedForwardDampner;
//        }

        rightFlywheel.set(currentRightPower);
        leftFlywheel.set(currentLeftPower);

        if(MainTeleop.Telemetry.ShooterTelemetry) {
            telemetry.addData("target", this.targetVelocity);
            telemetry.addData("feedforward timer", elaspedTime);

            telemetry.addData("right velocity", rightVelocity);
            telemetry.addData("right error", rightError);
            telemetry.addData("right power delta", rightPowerDelta);
            telemetry.addData("right feedforward boost", currentBoost);
            telemetry.addData("current right power", currentRightPower);

            telemetry.addData("left velocity", leftVelocity);
            telemetry.addData("left error", leftError);
            telemetry.addData("left power delta", leftPowerDelta);
            telemetry.addData("left feedforward boost", currentBoost);
            telemetry.addData("current left power", currentLeftPower);

            telemetry.update();
        }
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

    public void Shoot() {

    }

    public void CloseShoot() {
        this.liftServo.setPosition(ShooterConfig.TiltServoHi);
        this.targetVelocity = ShooterConfig.ShooterRpmLo;
    }

    public void CloseShootWithScale(double scale, double elevationScale) {
        this.liftServo.setPosition(ShooterConfig.TiltServoHi*elevationScale);
        this.targetVelocity = ShooterConfig.ShooterRpmLo * scale;
    }

    public void FarShoot() {
        this.liftServo.setPosition(ShooterConfig.TiltServoLo);
        this.targetVelocity = ShooterConfig.ShooterRpmHi;
    }

    ElapsedTime feedforwardTimer = new ElapsedTime();

    public void BeginFeedForwardBoost() {
        //feedforwardTimer.reset();
        //currentBoost = ShooterFeedforwardConfig.FeedforwardBoost;
    }

    public void FarShootWithScale(double scale,double elevationScale) {
        this.liftServo.setPosition(ShooterConfig.TiltServoLo*elevationScale);
        this.targetVelocity = ShooterConfig.ShooterRpmHi * scale;
    }

    public boolean isReadyToShoot() {
        return wasLastColorGreen;
    }
}
