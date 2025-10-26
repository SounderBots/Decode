package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

import static com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;

public class LimeLightSubsystem extends SubsystemBase {

    private Limelight3A limelight;

    protected Telemetry telemetry;

    private static final int PIPELINE_ID = 4; // April tag pipeline id

    private static final double CAMERA_HEIGHT = 0.5; // meters
    private static final double TARGET_HEIGHT = 1.0; // meters (center of AprilTag)
    private static final double CAMERA_MOUNT_ANGLE = 15.0; // degrees

    //Needs to be removed
    @Override
    public void periodic() {
        super.periodic();

        getTagID();
        getDistance();
    }
    public LimeLightSubsystem(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        start();
    }

    public int getTagID(){
        FiducialResult fr = scanAprilTag();
        int tagID = 0;
        if (fr != null) {
            tagID = fr.getFiducialId();
            telemetry.addData("Tag ID", tagID);
        } else {
            telemetry.addData("Limelight", "No AprilTags detected");
        }

        return tagID;
    }

    public double getDistance(){
        FiducialResult fr = scanAprilTag();
        double distance = 0d;
        if (fr != null) {
            int tagID = fr.getFiducialId();
//            double xDeg = fr.getTargetXDegrees(); // horizontal offset
//            double yDeg = fr.getTargetYDegrees();

            Pose3D pose3D = fr.getCameraPoseTargetSpace();
            Position p = pose3D.getPosition();

            double x = p.x;
            double y = p.y;
            double z = p.z;

            distance = Math.sqrt(x*x + y*y + z*z);

//            double ty = yDeg; // vertical offset in degrees
//            distance = estimateDistance(CAMERA_HEIGHT, TARGET_HEIGHT, CAMERA_MOUNT_ANGLE, ty);

            telemetry.addData("Tag ID", tagID);
            //telemetry.addData("Horizontal Angle", "%.2f deg", xDeg);
            //telemetry.addData("Vertical Angle", "%.2f deg", yDeg);
            telemetry.addData("Estimated Distance", "%.2f meters", distance);
            ;
        } else {
            telemetry.addData("Limelight", "No AprilTags detected");
        }
        telemetry.update();
        return distance;
    }

    public void scanObjects(){

    }

    private FiducialResult scanAprilTag() {

        // Set AprilTag pipeline
        limelight.pipelineSwitch(PIPELINE_ID);
        LLResult result = limelight.getLatestResult();

        if (result != null && result.isValid()) {
            FiducialResult fr =  result.getFiducialResults().get(0);;
            return fr;
        }

        return null;
    }


    public void start(){
        limelight.start();
        telemetry.addData(">", "Robot Ready. Press Play");
        telemetry.update();
        // Optional: reduce telemetry update frequency
        telemetry.setMsTransmissionInterval(20);

    }

   //  // AprilTag pipeline number

//    @Override
//    public void runOpMode() throws InterruptedException {
////        // Initialize Limelight from hardware map
////        limelight = hardwareMap.get(Limelight3A.class, "limelight");
//
//
//
//        // Start polling the Limelight
//
//
//
//        while (opModeIsActive()) {
//            LLResult result = limelight.getLatestResult();
//
//            if (result != null && result.isValid()) {
//                // Loop through all detected AprilTags
//                for (LLResultTypes.FiducialResult fr : result.getFiducialResults()) {
//                    int tagID = fr.getFiducialId();
//                    double xDeg = fr.getTargetXDegrees(); // horizontal offset
//                    double yDeg = fr.getTargetYDegrees(); // vertical offset
//
//                    // Estimate distance using vertical offset
//                    double ty = yDeg; // vertical offset in degrees
//                    double distance = estimateDistance(CAMERA_HEIGHT, TARGET_HEIGHT, CAMERA_MOUNT_ANGLE, ty);
//
//                    telemetry.addData("Tag ID", tagID);
//                    telemetry.addData("Horizontal Angle", "%.2f deg", xDeg);
//                    telemetry.addData("Vertical Angle", "%.2f deg", yDeg);
//                    telemetry.addData("Estimated Distance", "%.2f meters", distance);
//                }
//            } else {
//                telemetry.addData("Limelight", "No AprilTags detected");
//            }
//
//            telemetry.update();
//        }
//
//        limelight.stop();
//    }

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