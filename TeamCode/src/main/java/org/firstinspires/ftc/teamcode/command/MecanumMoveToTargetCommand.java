package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.arcrobotics.ftclib.controller.PIDController;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class MecanumMoveToTargetCommand extends SounderBotCommandBase {
    private static final String LOG_TAG = MecanumMoveToTargetCommand.class.getSimpleName();

    // Hardware components
    private final MecanumDrive mecanumDrive;
    private final GoBildaPinpointDriver pinpoint;

    // Target pose
    private final Pose2d targetPose;

    // PID Controllers
    private final PIDController xController;
    private final PIDController yController;
    private final PIDController thetaController;

    // Tolerance values
    private final double positionTolerance; // inches
    private final double angleTolerance;    // radians

    // Speed limits
    private final double maxSpeed;

    // Debugging
    private final boolean debugMode;

    public MecanumMoveToTargetCommand(
            MecanumDrive mecanumDrive,
            GoBildaPinpointDriver pinpoint,
            Pose2d targetPose,
            long timeoutMs) {
        this(mecanumDrive, pinpoint, targetPose, timeoutMs,
                0.08, 0.0, 0.008,  // X PID
                0.08, 0.0, 0.0008,  // Y PID
                1.5, 0.0, 0.08,   // Theta PID
                1.0,             // Position tolerance (inches)
                Math.toRadians(5), // Angle tolerance (5 degrees)
                0.8,             // Max speed
                false);          // Debug mode
    }

    public MecanumMoveToTargetCommand(
            MecanumDrive mecanumDrive,
            GoBildaPinpointDriver pinpoint,
            Pose2d targetPose,
            long timeoutMs,
            double xP, double xI, double xD,
            double yP, double yI, double yD,
            double thetaP, double thetaI, double thetaD,
            double positionTolerance,
            double angleTolerance,
            double maxSpeed,
            boolean debugMode) {

        super(timeoutMs);

        this.mecanumDrive = mecanumDrive;
        this.pinpoint = pinpoint;
        this.targetPose = targetPose;
        this.positionTolerance = positionTolerance;
        this.angleTolerance = angleTolerance;
        this.maxSpeed = maxSpeed;
        this.debugMode = debugMode;

        // Initialize PID controllers
        this.xController = new PIDController(xP, xI, xD);
        this.yController = new PIDController(yP, yI, yD);
        this.thetaController = new PIDController(thetaP, thetaI, thetaD);

        // Set PID setpoints
        this.xController.setSetPoint(targetPose.getX());
        this.yController.setSetPoint(targetPose.getY());
        this.thetaController.setSetPoint(targetPose.getRotation().getRadians());
    }

    @Override
    public void initialize() {
        super.initialize();

        // Update pinpoint and log starting position
        pinpoint.update();
        Pose2d currentPose = getCurrentPose();

        Log.i(LOG_TAG, String.format("Starting move from (%.2f, %.2f, %.1f°) to (%.2f, %.2f, %.1f°)",
                currentPose.getX(), currentPose.getY(), Math.toDegrees(currentPose.getRotation().getRadians()),
                targetPose.getX(), targetPose.getY(), Math.toDegrees(targetPose.getRotation().getRadians())));
    }

    @Override
    protected void doExecute() {
        // Update odometry
        pinpoint.update();

        // Get current pose
        Pose2d currentPose = getCurrentPose();

        // Calculate PID outputs
        double xOutput = xController.calculate(currentPose.getX());
        double yOutput = yController.calculate(currentPose.getY());

        // Handle angle wrapping manually for theta controller
        double currentAngle = currentPose.getRotation().getRadians();
        double targetAngle = targetPose.getRotation().getRadians();
        double angleError = normalizeAngle(targetAngle - currentAngle);
        double rotOutput = thetaController.calculate(currentAngle, currentAngle + angleError);

        // Limit speeds
        xOutput = Math.max(-maxSpeed, Math.min(maxSpeed, xOutput));
        yOutput = Math.max(-maxSpeed, Math.min(maxSpeed, yOutput));
        rotOutput = Math.max(-maxSpeed, Math.min(maxSpeed, rotOutput));

        // Drive the robot (robot-centric)
//        mecanumDrive.driveRobotCentric(xOutput, yOutput, rotOutput, false);
        mecanumDrive.driveFieldCentric(xOutput, yOutput, rotOutput, currentAngle, false);

//        // Debug logging
//        onFlagEnabled(debugMode, () -> {
//            double xError = targetPose.getX() - currentPose.getX();
//            double yError = targetPose.getY() - currentPose.getY();
//
//
//            //noinspection ReassignedVariable
//            Log.d(LOG_TAG, String.format("Current: (%.2f, %.2f, %.1f°) | Errors: (%.2f, %.2f, %.1f°) | Outputs: (%.2f, %.2f, %.2f)",
//                    currentPose.getX(), currentPose.getY(), Math.toDegrees(currentAngle),
//                    xError, yError, Math.toDegrees(angleError),
//                    xOutput, yOutput, rotOutput));
//        });
    }

    @Override
    protected boolean isTargetReached() {
        pinpoint.update();
        Pose2d currentPose = getCurrentPose();

        // Calculate errors
        double xError = Math.abs(targetPose.getX() - currentPose.getX());
        double yError = Math.abs(targetPose.getY() - currentPose.getY());
        double positionError = Math.sqrt(xError * xError + yError * yError);

        double currentAngle = currentPose.getRotation().getRadians();
        double targetAngle = targetPose.getRotation().getRadians();
        double angleError = Math.abs(normalizeAngle(targetAngle - currentAngle));

        boolean positionReached = positionError <= positionTolerance;
        boolean angleReached = angleError <= angleTolerance;

        if (positionReached && angleReached) {
            Log.i(LOG_TAG, String.format("Target reached! Position error: %.2f inches, Angle error: %.1f degrees",
                    positionError, Math.toDegrees(angleError)));
            return true;
        }

        return false;
    }

    @Override
    public void end(boolean interrupted) {
        // Stop the robot
        mecanumDrive.stop();

        if (interrupted) {
            Log.w(LOG_TAG, "MecanumMoveToTargetCommand was interrupted");
        } else {
            Log.i(LOG_TAG, "MecanumMoveToTargetCommand completed successfully");
        }

        super.end(interrupted);
    }

    @Override
    protected boolean isDebugging() {
        return debugMode;
    }

    /**
     * Normalize angle to [-π, π] range
     */
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    private Pose2d getCurrentPose() {
        // Convert pinpoint data to FTCLib Pose2d (mm to inches)
        double x = pinpoint.getPosX(DistanceUnit.INCH); // mm to inches
        double y = pinpoint.getPosY(DistanceUnit.INCH); // mm to inches
        double heading = pinpoint.getHeading(AngleUnit.RADIANS);

        return new Pose2d(x, y, new Rotation2d(heading));
    }
}
