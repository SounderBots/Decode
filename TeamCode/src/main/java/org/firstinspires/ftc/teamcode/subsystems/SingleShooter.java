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

    SonicPIDFController rightShooterPid = new SonicPIDFController(ShooterConfig.kP, 0, 0);
    SonicPIDFController leftShooterPid = new SonicPIDFController(ShooterConfig.kP, 0, 0);

    @Config
    public static class ShooterConfig {

        public static double ShooterRpmHi = 1800;

        public static double ShooterRpmLo = 1200;

        public static double RightLauncherStow = 0.34;

        public static double LeftLauncherStow = 0.53;

        public static double FeederShoot = .4;

        public static double IntakeMaxPower = .7;

        public static double kP = 0.000125;

        public static double kI = 0.0001;

        public static double kD = 0.00005;
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

        rightFlywheel.setVeloCoefficients(ShooterConfig.kP, ShooterConfig.kI, ShooterConfig.kD);
        leftFlywheel.setVeloCoefficients(ShooterConfig.kP, ShooterConfig.kI, ShooterConfig.kD);
    }

    double currentLeftPower = 0.63;
    double currentRightPower = 0.63;


    @Override
    public void periodic() {
        super.periodic();

        double rightError = targetVelocity - rightFlywheel.getVelocity();
        double rightPowerDelta = rightShooterPid.calculatePIDAlgorithm(rightError);

        double leftError = targetVelocity - leftFlywheel.getVelocity();
        double leftPowerDelta = leftShooterPid.calculatePIDAlgorithm(leftError);

        currentRightPower += rightPowerDelta;
        currentLeftPower += leftPowerDelta;

        rightFlywheel.set(currentRightPower);
        leftFlywheel.set(currentLeftPower);

        boolean addTelemetry = true;
        if(addTelemetry) {
            telemetry.addData("target", this.targetVelocity);

            telemetry.addData("right velocity", rightFlywheel.getVelocity());
            telemetry.addData("right error", rightError);
            telemetry.addData("right power delta", rightPowerDelta);
            telemetry.addData("current right power", currentRightPower);

            telemetry.addData("left velocity", leftFlywheel.getVelocity());
            telemetry.addData("left error", leftError);
            telemetry.addData("left power delta", leftPowerDelta);
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
}
