package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.util.SonicPIDFController;

public class SingleShooter extends SubsystemBase {

    Telemetry telemetry;
    GamepadEx gamepad;
    MotorEx leftFlywheel, rightFlywheel;

    double shooterCpr;

    SonicPIDFController rightShooterPid = new SonicPIDFController(.01, 0, 0);
    SonicPIDFController leftShooterPid = new SonicPIDFController(.01, 0, 0);

    @Config
    public static class ShooterConfig {

        public static double ShooterRpm = 1200;

        public static double RightLauncherStow = 0.34;

        public static double LeftLauncherStow = 0.53;

        public static double FeederShoot = .4;

        public static double IntakeMaxPower = .7;

        public static double kP = 20;
    }

    public SingleShooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;

        this.rightFlywheel = new MotorEx(hardwareMap, "RightFlywheel", Motor.GoBILDA.BARE);
        this.leftFlywheel = new MotorEx(hardwareMap, "LeftFlywheel", Motor.GoBILDA.BARE);

        this.rightFlywheel.setRunMode(Motor.RunMode.RawPower);
        this.leftFlywheel.setRunMode(Motor.RunMode.RawPower);

        this.leftFlywheel.setInverted(true);

        this.rightFlywheel.setZeroPowerBehavior( Motor.ZeroPowerBehavior.FLOAT);
        this.leftFlywheel.setZeroPowerBehavior( Motor.ZeroPowerBehavior.FLOAT);

        rightFlywheel.setVeloCoefficients(ShooterConfig.kP, 0, 0);
        leftFlywheel.setVeloCoefficients(ShooterConfig.kP, 0, 0);
    }

    @Override
    public void periodic() {
        super.periodic();

        double rightError = targetVelocity - rightFlywheel.getVelocity();
        double rightPower = rightShooterPid.calculatePIDAlgorithm(rightError);

        double leftError = targetVelocity - leftFlywheel.getVelocity();
        double leftPower = leftShooterPid.calculatePIDAlgorithm(leftError);

        rightFlywheel.set(rightPower);
        leftFlywheel.set(leftPower);

        boolean addTelemetry = false;
        if(addTelemetry) {
            telemetry.addData("target", this.targetVelocity);

            telemetry.addData("right velocity", rightFlywheel.getVelocity());
            telemetry.addData("right error", rightError);
            telemetry.addData("right power", rightPower);

            telemetry.addData("left velocity", leftFlywheel.getVelocity());
            telemetry.addData("left error", leftError);
            telemetry.addData("left power", leftPower);

            telemetry.update();
        }
    }

    double targetTicksPerSecond = 0;

    double targetVelocity = 0;

    boolean toggleShooter = false;
    public void ToggleShooter() {
        if(toggleShooter) {
            this.targetTicksPerSecond = shooterCpr * ShooterConfig.ShooterRpm / 60;
            this.targetVelocity = ShooterConfig.ShooterRpm;
        } else {
            this.targetTicksPerSecond = 0;
        }

        toggleShooter = !toggleShooter;
    }
}
