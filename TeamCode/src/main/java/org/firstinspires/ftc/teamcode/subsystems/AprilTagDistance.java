package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "AprilTag Distance Demo", group = "Sensor")
public class AprilTagDistance extends LinearOpMode {

    private Limelight3A limelight;

    private static final double CAMERA_HEIGHT = 0.5; // meters
    private static final double TARGET_HEIGHT = 1.0; // meters (center of AprilTag)
    private static final double CAMERA_MOUNT_ANGLE = 15.0; // degrees
    private static final int PIPELINE_ID = 4; // AprilTag pipeline number

    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize Limelight from hardware map
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        // Optional: reduce telemetry update frequency
        telemetry.setMsTransmissionInterval(20);

        // Set AprilTag pipeline
        limelight.pipelineSwitch(PIPELINE_ID);

        // Start polling the Limelight
        limelight.start();

        telemetry.addData(">", "Robot Ready. Press Play");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            if (result != null && result.isValid()) {
                // Loop through all detected AprilTags
                for (LLResultTypes.FiducialResult fr : result.getFiducialResults()) {
                    int tagID = fr.getFiducialId();
                    double xDeg = fr.getTargetXDegrees(); // horizontal offset
                    double yDeg = fr.getTargetYDegrees(); // vertical offset

                    // Estimate distance using vertical offset
                    double ty = yDeg; // vertical offset in degrees
                    double distance = estimateDistance(CAMERA_HEIGHT, TARGET_HEIGHT, CAMERA_MOUNT_ANGLE, ty);

                    telemetry.addData("Tag ID", tagID);
                    telemetry.addData("Horizontal Angle", "%.2f deg", xDeg);
                    telemetry.addData("Vertical Angle", "%.2f deg", yDeg);
                    telemetry.addData("Estimated Distance", "%.2f meters", distance);
                }
            } else {
                telemetry.addData("Limelight", "No AprilTags detected");
            }

            telemetry.update();
        }

        limelight.stop();
    }

    /**
     * Estimate distance to target using camera height, target height,
     * camera mounting angle, and vertical offset (ty)
     */
    private double estimateDistance(double cameraHeight, double targetHeight, double cameraAngleDeg, double verticalOffsetDeg) {
        double a1 = Math.toRadians(cameraAngleDeg);
        double a2 = Math.toRadians(verticalOffsetDeg);
        return (targetHeight - cameraHeight) / Math.tan(a1 + a2);
    }
}