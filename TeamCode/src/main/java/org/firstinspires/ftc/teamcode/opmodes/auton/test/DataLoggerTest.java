package org.firstinspires.ftc.teamcode.opmodes.auton.test;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.controller.PIDFController;
import com.arcrobotics.ftclib.controller.wpilibcontroller.SimpleMotorFeedforward;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.datalogger.DataLogger;

import java.util.ArrayList;
import java.util.List;

@Config
@Autonomous(name = "DataLogger Test", group = "Test")
public class DataLoggerTest extends LinearOpMode {

    private MotorEx testMotor;
    private DataLogger logger;
    private ElapsedTime timer;

    public static double TargetVelocity = 0;
    public static boolean InvertMotor = true;
    public static String MotorName = "RightFlywheel"; // Default to shooter motor

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
        
        // Display all available motors to help user choose
        telemetry.addData("Instructions", "Check the list below and set 'MotorName' in Dashboard");
        telemetry.addData("Instructions", "Then press START.");
        telemetry.addLine("--- Available Motors ---");
        
        List<String> allMotorNames = new ArrayList<>();
        for (DcMotorEx motor : hardwareMap.getAll(DcMotorEx.class)) {
            String name = hardwareMap.getNamesOf(motor).iterator().next();
            allMotorNames.add(name);
            
            // Force FLOAT mode so user can spin it by hand to check
            motor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
            
            telemetry.addData("Motor", name + " (Port: " + motor.getPortNumber() + ")");
        }
        telemetry.update();

        // Wait for user to set config and press start
        while (!isStarted() && !isStopRequested()) {
            telemetry.addData("Target Motor", MotorName);
            telemetry.addData("Status", "Waiting for Start...");
            telemetry.addLine("--- Available Motors ---");
            for (String name : allMotorNames) {
                telemetry.addData("Found", name);
            }
            telemetry.update();
            sleep(100);
        }

        if (isStopRequested()) return;

        String motorNameToUse = MotorName;
        
        try {
            // Try to find the specific motor first
            try {
                hardwareMap.get(DcMotorEx.class, motorNameToUse);
            } catch (Exception e) {
                // If not found, default to the first one in the list
                if (!allMotorNames.isEmpty()) {
                    motorNameToUse = allMotorNames.get(0);
                    telemetry.addData("Warning", "Motor '" + MotorName + "' not found. Defaulting to: " + motorNameToUse);
                }
            }
            
            if (motorNameToUse == null) {
                throw new RuntimeException("No motors found in configuration!");
            }

            telemetry.addData("Motor Found", "Using motor: " + motorNameToUse);
            telemetry.update();

            // Initialize MotorEx with the found name and GoBILDA BARE type
            testMotor = new MotorEx(hardwareMap, motorNameToUse, Motor.GoBILDA.BARE);
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

        telemetry.addData("Status", "Running");
        telemetry.addData("Log File", logFileName);
        telemetry.addData("Storage Path", DataLogger.StoragePath);
        telemetry.addData("Log To SD", DataLogger.LogToSdCard);
        telemetry.update();

        try {
            if (opModeIsActive()) {
                // Start logging with headers (Matched to Shooter.java naming)
                logger.startLogging("TargetTPS", "ActualTPS", "Error", "PidPower", "FfPower", "TotalPower", "EncoderPos");
                
                // Log the PIDF constants at the start of the file (Matched to Shooter.java metadata)
                logger.logComment("PIDF Config: kP=" + kP + " kI=" + kI + " kD=" + kD);
                logger.logComment("Feedforward Config: kS=" + ks + " kV=" + kv + " kA=" + ka);
                
                timer = new ElapsedTime();
                
                // Run indefinitely until stop is pressed
                while (opModeIsActive()) {
                    double currentTime = timer.seconds();

                    // Set target velocity based on time pattern (looping every 12 seconds)
                    double loopTime = currentTime % 12.0;
                    
                    if (loopTime < 4.0) {
                        TargetVelocity = 300; // Ramp up
                    } else if (loopTime < 8.0) {
                        TargetVelocity = 150; // Drop
                    } else {
                        TargetVelocity = 400; // Increase again
                    }

                    // Update PID coefficients from Dashboard (if changed)
                    pidController.setPIDF(kP, kI, kD, 0.0);
                    feedforward = new SimpleMotorFeedforward(ks, kv, ka);

                    // Calculate Control
                    double currentVelocity = testMotor.getVelocity();
                    double error = TargetVelocity - currentVelocity;

                    double pidPower = pidController.calculate(currentVelocity, TargetVelocity);
                    // Note: calculate(velocity) ignores ka. To use ka, we need calculate(vel, accel).
                    // Shooter.java currently uses calculate(velocity), so we match that here.
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
                        totalPower,
                        testMotor.getCurrentPosition()
                    );

                    telemetry.addData("Time", "%.2f", currentTime);
                    telemetry.addData("Target TPS", TargetVelocity);
                    telemetry.addData("Actual TPS", currentVelocity);
                    telemetry.addData("Encoder Pos", testMotor.getCurrentPosition()); // Debug for 0 velocity issue
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
