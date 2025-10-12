package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.arcrobotics.ftclib.controller.PIDController;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import lombok.Builder;

@Builder
public class MecanumMoveToTargetCommand extends SounderBotCommandBase {
    private static final String LOG_TAG = MecanumMoveToTargetCommand.class.getSimpleName();
    public static final double POSITION_TOLERANCE = 1.0;
    public static final double ANGLE_TOLERANCE = Math.toRadians(5);
    public static final double MAX_SPEED = 0.8;
    public static final boolean DEBUG_MODE = false;

    // Hardware components
    private final MecanumDrive mecanumDrive;
    private final DcMotor odoX;  // Lateral encoder
    private final DcMotor odoY;  // Forward encoder
    private final IMU imu;       // For heading

    // Current pose tracking
    private Pose2d currentPose;
    private int lastXTicks = 0;
    private int lastYTicks = 0;

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

    // production page: https://www.gobilda.com/swingarm-odometry-pod-48mm-wheel/?srsltid=AfmBOoqqFvQcj2Bgy26Rpc5jwVFo493R3sK4nwqGkpn9q1o2p7Dso8Vs
    // Circumference (mm): C = π × 48 = 150.796 mm
    // Ticks per mm: 2000 / 150.796 ≈ 13.263 ticks/mm
    // Ticks per inch: 13.263 × 25.4 ≈ 336.9 ticks/in
    public static final double TICKS_PER_MM = 2000.0 / (Math.PI * 48.0); // ≈ 13.263
    public static final double TICKS_PER_INCH = TICKS_PER_MM * 25.4; // ≈ 336.9

    private static final PIDController X_CTRL = new PIDController(0.08, 0.0, 0.008);
    private static final PIDController Y_CTRL = new PIDController(0.08, 0.0, 0.0008);
    private static final PIDController THETA_CTRL = new PIDController(1.5, 0.0, 0.08);

    public MecanumMoveToTargetCommand(
            MecanumDrive mecanumDrive,
            DcMotor odoX,
            DcMotor odoY,
            IMU imu,
            Pose2d targetPose,
            long timeoutMs) {

        super(timeoutMs);

        this.mecanumDrive = mecanumDrive;
        this.odoX = odoX;
        this.odoY = odoY;
        this.imu = imu;
        this.targetPose = targetPose;
        this.positionTolerance = POSITION_TOLERANCE;
        this.angleTolerance = ANGLE_TOLERANCE;
        this.maxSpeed = MAX_SPEED;
        this.debugMode = DEBUG_MODE;

        // Initialize current pose to origin
        this.currentPose = new Pose2d(0, 0, new Rotation2d(0));

        // Initialize PID controllers
        this.xController = new PIDController(X_CTRL.getP(), X_CTRL.getI(), X_CTRL.getD());
        this.yController = new PIDController(Y_CTRL.getP(), Y_CTRL.getI(), Y_CTRL.getD());
        this.thetaController = new PIDController(THETA_CTRL.getP(), THETA_CTRL.getI(), THETA_CTRL.getD());

        // Set PID setpoints
        this.xController.setSetPoint(targetPose.getX());
        this.yController.setSetPoint(targetPose.getY());
        this.thetaController.setSetPoint(targetPose.getRotation().getRadians());
    }

    @Override
    public void initialize() {
        super.initialize();

        // Reset encoders
        lastXTicks = odoX.getCurrentPosition();
        lastYTicks = odoY.getCurrentPosition();

        // Update and log starting position
        updateOdometry();
        Pose2d currentPose = getCurrentPose();

        Log.i(LOG_TAG, String.format("Starting move from (%.2f, %.2f, %.1f°) to (%.2f, %.2f, %.1f°)",
                currentPose.getX(), currentPose.getY(), Math.toDegrees(currentPose.getRotation().getRadians()),
                targetPose.getX(), targetPose.getY(), Math.toDegrees(targetPose.getRotation().getRadians())));
    }

    @Override
    protected void doExecute() {
        // Update odometry
        updateOdometry();

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

        // Drive the robot (field-centric)
        mecanumDrive.driveFieldCentric(xOutput, yOutput, rotOutput, currentAngle, false);
    }

    @Override
    protected boolean isTargetReached() {
        updateOdometry();
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

    /**
     * Update odometry based on encoder deltas
     */
    private void updateOdometry() {
        // Read current encoder positions
        int currentXTicks = odoX.getCurrentPosition();
        int currentYTicks = odoY.getCurrentPosition();

        // Calculate deltas
        int deltaXTicks = currentXTicks - lastXTicks;
        int deltaYTicks = currentYTicks - lastYTicks;

        // Convert to inches
        double deltaX = deltaXTicks / TICKS_PER_INCH;
        double deltaY = deltaYTicks / TICKS_PER_INCH;

        // Get current heading from IMU
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);

        // Transform deltas from robot frame to field frame
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);

        double fieldDeltaX = deltaX * cos - deltaY * sin;
        double fieldDeltaY = deltaX * sin + deltaY * cos;

        // Update pose
        currentPose = new Pose2d(
                currentPose.getX() + fieldDeltaX,
                currentPose.getY() + fieldDeltaY,
                new Rotation2d(heading)
        );

        // Update last tick counts
        lastXTicks = currentXTicks;
        lastYTicks = currentYTicks;
    }

    private Pose2d getCurrentPose() {
        return currentPose;
    }
}