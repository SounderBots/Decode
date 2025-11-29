package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.arcrobotics.ftclib.controller.wpilibcontroller.SimpleMotorFeedforward;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.DataLogger;

@Config
@Autonomous(name = "DataLogger Test", group = "Test")
public class DataLoggerTest extends LinearOpMode {

    private MotorEx testMotor;
    private DataLogger logger;
    private ElapsedTime timer;

    public static double TargetVelocity = 0;
    public static boolean InvertMotor = true;

    // PID coefficients from Shooter.java
    public static double kP = 0.0095;
    public static double kI = 0.005;
    public static double kD = 0;

    // Feedforward coefficients from Shooter.java
    public static double ks = 0;
    public static double kv = 0.00032; // Using kv_left as default
    public static double ka = .01;

    @Override
    public void runOpMode() {
        // 1. Initialize Hardware
        // Find ANY motor configured in the hardware map
        String motorName = null;
        try {
            // Get all configured devices
            for (DcMotorEx motor : hardwareMap.getAll(DcMotorEx.class)) {
                // Just take the first one we find
                motorName = hardwareMap.getNamesOf(motor).iterator().next();
                telemetry.addData("Motor Found", "Using motor: " + motorName);
                break;
            }
            
            if (motorName == null) {
                throw new RuntimeException("No motors found in configuration!");
            }

            // Initialize MotorEx with the found name and GoBILDA BARE type
            testMotor = new MotorEx(hardwareMap, motorName, Motor.GoBILDA.BARE);
            testMotor.setRunMode(Motor.RunMode.RawPower);
            testMotor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
            testMotor.setInverted(InvertMotor); // Configurable inversion

        } catch (Exception e) {
            telemetry.addData("Error", "Could not initialize motor: " + e.getMessage());
            telemetry.update();
            sleep(5000);
            return;
        }

        // 2. Initialize DataLogger
        String logFileName = DataLogger.getLogFileName("DataLoggerTest", "PID_Test");
        logger = new DataLogger(logFileName);

        // Initialize PID and Feedforward
        PIDFController pidController = new PIDFController(kP, kI, kD, 0.0);
        SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(ks, kv, ka);

        telemetry.addData("Status", "Initialized");
        telemetry.addData("Log File", logFileName);
        telemetry.addData("Storage Path", DataLogger.StoragePath);
        telemetry.addData("Log To SD", DataLogger.LogToSdCard);
        telemetry.update();

        waitForStart();

        try {
            if (opModeIsActive()) {
                // Start logging with headers
                logger.startLogging("TargetVelocity", "ActualVelocity", "Error", "PidPower", "FfPower", "TotalPower");
                
                timer = new ElapsedTime();
                
                while (opModeIsActive() && timer.seconds() < 15) {
                    double currentTime = timer.seconds();

                    // Set target velocity based on time pattern
                    if (currentTime < 4.0) {
                        TargetVelocity = 300; // Ramp up immediately
                    } else if (currentTime < 8.0) {
                        TargetVelocity = 150; // Drop
                    } else if (currentTime < 12.0) {
                        TargetVelocity = 400; // Increase again
                    } else {
                        TargetVelocity = 0;
                    }

                    // Update PID coefficients from Dashboard (if changed)
                    pidController.setPIDF(kP, kI, kD, 0.0);
                    feedforward = new SimpleMotorFeedforward(ks, kv, ka);

                    // Calculate Control
                    double currentVelocity = testMotor.getVelocity();
                    double error = TargetVelocity - currentVelocity;

                    double pidPower = pidController.calculate(currentVelocity, TargetVelocity);
                    double ffPower = feedforward.calculate(TargetVelocity);
                    
                    double totalPower = pidPower + ffPower;
                    
                    // Clamp power
                    totalPower = Math.max(-1.0, Math.min(1.0, totalPower));

                    testMotor.set(totalPower);

                    // Log data
                    logger.log(
                        TargetVelocity,
                        currentVelocity,
                        error,
                        pidPower,
                        ffPower,
                        totalPower
                    );

                    telemetry.addData("Time", "%.2f", currentTime);
                    telemetry.addData("Target Vel", TargetVelocity);
                    telemetry.addData("Actual Vel", currentVelocity);
                    telemetry.addData("Power", "%.2f", totalPower);
                    telemetry.update();
                }
            }
        } finally {
            // 3. Close Logger
            logger.close();
            if (testMotor != null) {
                testMotor.set(0);
            }
        }
    }
}
