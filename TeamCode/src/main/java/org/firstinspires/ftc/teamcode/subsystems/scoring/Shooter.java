package org.firstinspires.ftc.teamcode.subsystems.scoring;

import android.util.Log;

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
import org.firstinspires.ftc.teamcode.common.AprilTagPosition;
import org.firstinspires.ftc.teamcode.datalogger.DataLogger;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;
import org.firstinspires.ftc.teamcode.util.WifiMonitor;

public class Shooter extends SubsystemBase {

    private static final String LOG_TAG = Shooter.class.getSimpleName();
    Telemetry telemetry;
    GamepadEx gamepad;
    MotorEx leftFlywheel, rightFlywheel;

    Servo liftServo;

    RGBLightIndicator speedIndicator;
    DataLogger logger;
    WifiMonitor wifiMonitor;

    @Config
    public static class ShooterConfig {

        public static double ShooterTpsHi = 720;

        public static double ShooterTpsLo = 825;

        public static double RightLauncherStow = 0.34;

        public static double LeftLauncherStow = 0.53;

        public static double FeederShoot = .4;

        public static double FeederReset = .9;

        public static double IntakeMaxPower = 1;

        public static double TiltServoHi = 0.95;

        public static double TiltServoLo = 0.95;

        public static double FlywheelAcceptableTpsError = 40;

        public static long AutoSpeedCheckSkipCount = 10;

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

    LimeLightAlign limelight;

    boolean isDemoMode = false;

    public static final String[] LOG_COLUMNS = {
            "ShooterReady", "IsShooting", "TargetTPS", "Tilt",
            "RightTPS", "RightError", "RightPowerPID", "RightPowerFF", "RightPower",
            "LeftTPS", "LeftError", "LeftPowerPID", "LeftPowerFF", "LeftPower",
            "RSSI", "LinkSpeed"
    };

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry, RGBLightIndicator speedIndicator) {
        this(hardwareMap, gamepad, telemetry, speedIndicator, null, "Shooter");
    }

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry, RGBLightIndicator speedIndicator, LimeLightAlign limelight) {
        this(hardwareMap, gamepad, telemetry, speedIndicator, limelight, "Shooter");
    }

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry, RGBLightIndicator speedIndicator, LimeLightAlign limelight, String opModeName) {
        this(hardwareMap, gamepad, telemetry, speedIndicator, limelight, "Shooter", false);

    }

    public Shooter(HardwareMap hardwareMap, GamepadEx gamepad, Telemetry telemetry, RGBLightIndicator speedIndicator, LimeLightAlign limelight, String opModeName, boolean isDemoMode) {
        this.gamepad = gamepad;
        this.telemetry = telemetry;
        this.limelight = limelight;
        this.isDemoMode = isDemoMode;

        if(isDemoMode) {
            autoSpeed = true;
        }

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

        wifiMonitor = new WifiMonitor();

        logger = new DataLogger(DataLogger.getLogFileName(opModeName, "ShooterLog"));
        logger.initializeLogging(LOG_COLUMNS);

        // Log the PIDF constants at the start of the file
        logger.logComment("PIDF Config: kP=" + ShooterControlConfig.kP + " kI=" + ShooterControlConfig.kI + " kD=" + ShooterControlConfig.kD);
        logger.logComment("Feedforward Config: kS=" + ShooterControlConfig.ks + " kV_Left=" + ShooterControlConfig.kv_left + " kV_Right=" + ShooterControlConfig.kv_right + " kA=" + ShooterControlConfig.ka);

        if(MainTeleop.Telemetry.Shooter) {
            telemetry.addData("target velocity", this.targetVelocity);
            telemetry.addData("target tilt", this.targetVelocity);

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
    long counter = 0;
    double lastTilt = 0;
    boolean isShooting = false;

    public void SetShootingFlag() {
        isShooting = true;
    }

    @Override
    public void periodic() {
        super.periodic();

        // Don't check limelight every time.
        if(autoSpeed && counter++ == ShooterConfig.AutoSpeedCheckSkipCount) {
            AutoSpeed expectedSpeed = GetAutoSpeed();
            targetVelocity = expectedSpeed.Tps;

            if(expectedSpeed.Tilt != lastTilt) {
                liftServo.setPosition(expectedSpeed.Tilt + this.tiltDelta);
                lastTilt = expectedSpeed.Tilt;
            }

            counter = 0;
        }

        double rightVelocity = rightFlywheel.getVelocity();
        double rightError = targetVelocity - rightVelocity;

        double leftVelocity = leftFlywheel.getVelocity();
        double leftError = targetVelocity - leftVelocity;

        if(Math.abs(rightError) < ShooterConfig.FlywheelAcceptableTpsError &&
                Math.abs(leftError) < ShooterConfig.FlywheelAcceptableTpsError &&
                Math.abs(leftVelocity - rightVelocity) < 30)
        {
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

        if(!isDemoMode) {
            // Don't waste battery on flywheels during demos
            rightFlywheel.set(rightPower);
            leftFlywheel.set(leftPower);
        }

        logger.log(wasLastColorGreen ? 1 : 0, isShooting ? 1 : 0, targetVelocity, lastTilt, rightVelocity, rightError, rightPidPower, rightFeedforwardValue, rightPower, leftVelocity, leftError, leftPidPower, leftFeedforwardValue, leftPower, wifiMonitor.getSignalStrength(), wifiMonitor.getLinkSpeed());
        isShooting = false;

        if(MainTeleop.Telemetry.Shooter) {
            telemetry.addData("target velocity", this.targetVelocity);
            telemetry.addData("tilt", this.lastTilt);

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

    public class AutoSpeed {

        public AutoSpeed(double tps, double tilt) {
            this.Tps = tps;
            this.Tilt = tilt;
        }

        public double Tps;

        public double Tilt;
    }

    public AutoSpeed GetAutoSpeed() {
        AprilTagPosition position = this.limelight.getAprilTagPosition();

        AutoSpeed result = new AutoSpeed(695, 0.9);
        if(position != null) {
            double distance = position.distance();

            //y = 0.0101722*x^2 - 0.0456217*x + 672.12131
//            double tps = 0.0101722 * distance * distance - 0.0456217 * distance + 700; //672.12131;
//            double tilt = getTilt(distance);
            result = this.GetAutoSpeed(distance);
        }

        Log.i(LOG_TAG, "AutoSpeed: tps = " + result.Tps + ", tile: " + result.Tilt);
        telemetry.addData("AutoSpeed", "tps = " + result.Tps + ", tile: " + result.Tilt);
        telemetry.update();
        return result;
    }

    private AutoSpeed GetAutoSpeed(double distance) {
        if(!isDemoMode) {
            /*
            44 - 730, 0.95
            54 - 710, 0.95
            64 - 720, 0.95
            74 - 740, 0.95
            84 - 740, 0.95
            94 - 760, 0.95
            114 - 810, 0.95
            124 - 825, 0.95
            134 - 845, 0.95
            */
            if (distance < 44) {
                return new AutoSpeed(730, 0.95);
            } else if (distance < 54) {
                return new AutoSpeed(710, 0.95);
            } else if (distance < 64) {
                return new AutoSpeed(720, 0.95); // for auto short
            } else if (distance < 74) {
                return new AutoSpeed(740, 0.95);
            } else if (distance < 84) {
                return new AutoSpeed(740, 0.95);
            } else if (distance < 94) {
                return new AutoSpeed(760, 0.95);
            } else if (distance < 114) {
                return new AutoSpeed(810, 0.95);
            } else if (distance < 124) {
                return new AutoSpeed(825, 0.95);
            } else if (distance < 134) {
                return new AutoSpeed(845, 0.95);
            } else {
                return new AutoSpeed(720, 0.95);
            }
        } else {
            // Exaggerated tilt to help in demos
            if (distance < 44) {
                return new AutoSpeed( 695, 0.95);
            } else if (distance < 54) {
                return new AutoSpeed( 695, 0.85);
            } else if (distance < 64) {
                return new AutoSpeed( 695, 0.75);
            } else if (distance < 74) {
                return new AutoSpeed( 695, 0.65);
            } else if (distance < 84) {
                return new AutoSpeed( 695, 0.55);
            } else if (distance < 94) {
                return new AutoSpeed( 695, 0.45);
            } else if (distance < 114) {
                return new AutoSpeed( 695, 0.45);
            } else if (distance < 124) {
                return new AutoSpeed( 695, 0.45);
            } else if (distance < 134) {
                return new AutoSpeed( 695, 0.45);
            } else {
                return new AutoSpeed( 695, 0.85);
            }
        }
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    double targetVelocity = ShooterConfig.ShooterTpsHi;

    boolean toggleShooter = false;
    public void ToggleShooter() {
        if(toggleShooter) {
            this.targetVelocity = ShooterConfig.ShooterTpsLo;
        } else {
            this.targetVelocity = ShooterConfig.ShooterTpsHi;
        }

        toggleShooter = !toggleShooter;
    }

    public void SetTargetTps(double targetTps) {
        this.targetVelocity = targetTps;
    }

    public void CloseShoot() {
        this.liftServo.setPosition(ShooterConfig.TiltServoHi);
        this.targetVelocity = ShooterConfig.ShooterTpsLo;
        this.lastTilt = ShooterConfig.TiltServoHi;
    }

    public void CloseShootWithScale(double scale, double elevationScale) {
        this.liftServo.setPosition(ShooterConfig.TiltServoHi * elevationScale);
        this.targetVelocity = ShooterConfig.ShooterTpsLo * scale;
        this.lastTilt = ShooterConfig.TiltServoHi;
    }

    public void FarShoot() {
        this.liftServo.setPosition(ShooterConfig.TiltServoLo);
        this.targetVelocity = ShooterConfig.ShooterTpsHi;
        this.lastTilt = ShooterConfig.TiltServoLo;

    }

    public void FarShootWithScale(double scale, double elevationScale) {
        this.liftServo.setPosition(ShooterConfig.TiltServoLo * elevationScale);
        this.targetVelocity = ShooterConfig.ShooterTpsHi * scale;
        this.lastTilt = ShooterConfig.TiltServoLo;
    }

    public void autonShoot() {
        AutoSpeed targetSpeed = GetAutoSpeed();
        this.liftServo.setPosition(targetSpeed.Tilt);
        this.targetVelocity = targetSpeed.Tps;

    }

    public boolean isReadyToShoot() {
        return wasLastColorGreen;
    }

    boolean autoSpeed = false;

    public void AutoSpeedAndTilt() {
        if(this.limelight != null) {
            this.autoSpeed = true;
        }
    }

    public void DefaultSpeedAndTilt() {
        this.autoSpeed = false;
    }

    public void stopLogging() {
        if (logger != null) {
            logger.close();
        }
    }

    double tiltDelta = 0;

    public void HigherTilt() {
        this.tiltDelta += 0.05;
    }

    public void LowerTilt() {
        this.tiltDelta -= 0.05;
    }
}