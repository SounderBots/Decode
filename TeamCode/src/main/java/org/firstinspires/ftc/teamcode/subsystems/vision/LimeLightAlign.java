package org.firstinspires.ftc.teamcode.subsystems.vision;

import static com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.pedropathing.ftc.FTCCoordinates;
import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.command.CommonConstants;
import org.firstinspires.ftc.teamcode.common.AprilTagEnum;
import org.firstinspires.ftc.teamcode.common.AprilTagPosition;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.CameraOffsets;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.SpringTagPositions;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;

import java.util.List;
import java.util.Optional;

public class LimeLightAlign extends SubsystemBase {

    private static final String LOG_TAG = LimeLightAlign.class.getSimpleName();

    private Limelight3A limelight;

    protected Telemetry telemetry;

    private static final int PIPELINE_ID = 4; // April tag pipeline id

    private double horizontalAngle, verticalAngle;

    @Config
    public static class LimelightConfig {
        public static double leftSafeLimit = 2.5 + 2.0;

        public static double rightSafeLimit = 2.5 - 2.0;

        public static double leftOuterLimit = 2.5 + 3.5;

        public static double rightOuterLimit = 2.5 - 3.5;

    }

    RGBLightIndicator leftIndicator, rightIndicator;

    //Needs to be removed
    @Override
    public void periodic() {
        super.periodic();

        //getObeliskAprilTag();
        AprilTagPosition aprilTagPosition = getAprilTagPosition();

        if(aprilTagPosition == null) {
            leftIndicator.changeOff();
            rightIndicator.changeOff();
        }
        else {
            this.horizontalAngle = aprilTagPosition.horizontalAngle();

            if(horizontalAngle < LimelightConfig.leftSafeLimit && horizontalAngle > LimelightConfig.rightSafeLimit) {
                leftIndicator.changeGreen();
                rightIndicator.changeGreen();
            } else if (horizontalAngle > LimelightConfig.leftSafeLimit && horizontalAngle < LimelightConfig.leftOuterLimit) {
                leftIndicator.changeYellow();
                rightIndicator.changeGreen();
            } else if (horizontalAngle < LimelightConfig.rightSafeLimit && horizontalAngle > LimelightConfig.rightOuterLimit) {
                rightIndicator.changeYellow();
                leftIndicator.changeGreen();
            } else if (horizontalAngle > LimelightConfig.leftOuterLimit) {
                leftIndicator.changeRed();
                rightIndicator.changeGreen();
            } else if(horizontalAngle < LimelightConfig.rightOuterLimit) {
                leftIndicator.changeGreen();
                rightIndicator.changeRed();
            } else {
                leftIndicator.changeOff();
                rightIndicator.changeOff();
            }
        }
    }
    public LimeLightAlign(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        this.leftIndicator = new RGBLightIndicator(hardwareMap, telemetry, "LeftAlign");
        this.rightIndicator = new RGBLightIndicator(hardwareMap, telemetry, "RightAlign");

        start();
    }

    public Optional<AprilTagEnum> getObeliskAprilTag() {
        Optional<AprilTagEnum> result = scanObeliskTag().map(fiducialResult -> AprilTagEnum.fromValue(fiducialResult.getFiducialId()));
        if (result.isPresent()) {
            telemetry.addData("Scanned Obelisk Tag ID: ", result.get().getValue());
        } else {
            telemetry.addLine("Obelisk Tag not found");
        }
        return result;

//        FiducialResult fr = scanAprilTag();
//        AprilTagEnum aprilTagEnum = null;
//        if (fr != null) {
//            try {
//                aprilTagEnum = AprilTagEnum.fromValue(fr.getFiducialId());
//                if (AprilTagEnum.OBELISK_ALL.contains(aprilTagEnum)){
//                    telemetry.addData("Scanned Obelisk Tag ID: ", aprilTagEnum.getValue());
//                    aprilTagEnum = null;
//                } else {
//                    telemetry.addData("Not a valid ObelisK Tag ", aprilTagEnum.getValue());
//                }
//            } catch (Exception e) {
//                telemetry.addData("Limelight", "No Valid AprilTags detected");
//            }
//        } else {
//            telemetry.addData("Limelight", "No AprilTags detected");
//        }
//        telemetry.update();
//        return aprilTagEnum;
    }

    public AprilTagPosition getAprilTagPosition(){
        FiducialResult fr = scanAprilTag();
        AprilTagPosition aprilTagPosition = null;
        if (fr != null) {
            try {
                AprilTagEnum aprilTagEnum = null;
                aprilTagEnum = AprilTagEnum.fromValue(fr.getFiducialId());

                if(aprilTagEnum == AprilTagEnum.BLUE_GOAL || aprilTagEnum == AprilTagEnum.RED_GOAL) {

                    double distance = 0d;
                    Pose3D pose3D = fr.getTargetPoseCameraSpace();
                    Position p = pose3D.getPosition().toUnit(CommonConstants.DISTANCE_UNIT);

                    double x = p.x;
                    double y = p.y;
                    double z = p.z;

                    distance = Math.sqrt(x * x + y * y + z * z);

                    aprilTagPosition = new AprilTagPosition(aprilTagEnum, distance, fr.getTargetXDegrees(), fr.getTargetYDegrees());

                    boolean addTelemetry = MainTeleop.Telemetry.LimeLight;
                    if (addTelemetry) {
                        telemetry.addData("Tag ID", aprilTagEnum.getValue());
                        telemetry.addData("x", x);
                        telemetry.addData("y", y);
                        telemetry.addData("z", z);
                        telemetry.addData("Estimated Distance", aprilTagPosition.distance());
                        telemetry.addData("Estimated Horizontal shift", aprilTagPosition.horizontalAngle());
                        telemetry.addData("Estimated Vertical Shift", aprilTagPosition.verticalAngle());
                    }
                }
            } catch (Exception e) {
                telemetry.addData("Limelight", "No Valid AprilTags detected");
            }
        }
        telemetry.update();
        return aprilTagPosition;
    }

    public void scanObjects(){
    }

    public Pose getRobotPositionBasedOnSpringTag() {
        limelight.pipelineSwitch(PIPELINE_ID);
        LLResult result = limelight.getLatestResult();

        if (result != null && result.isValid()) {
            List<FiducialResult> fiducials = result.getFiducialResults();
            for (FiducialResult fr : fiducials) {
                int id = fr.getFiducialId();

                Pose tagMapPose = null;
                if (id == 20) {
                    tagMapPose = SpringTagPositions.RED;
                } else if (id == 24) {
                    tagMapPose = SpringTagPositions.BLUE;
                }

                if (tagMapPose != null) {
                    Log.i(LOG_TAG, "Tag ID: " + id);
                    telemetry.addData(LOG_TAG, "Tag ID: " + id);
                    Pose3D targetPose = fr.getTargetPoseCameraSpace();
                    Log.i(LOG_TAG, "targetPose: " + targetPose);
                    telemetry.addData(LOG_TAG, "targetPose: " + targetPose);

                    Pose3D targetPoseRobot = fr.getTargetPoseRobotSpace();
                    Log.i(LOG_TAG, "targetPoseRobot: " + targetPose);
                    telemetry.addData(LOG_TAG, "targetPoseRobot: " + targetPose);

                    Pose3D ftcRobotPose = fr.getRobotPoseFieldSpace();
                    Log.i(LOG_TAG, "ftcRobotPose: " + ftcRobotPose);
                    telemetry.addData(LOG_TAG, "ftcRobotPose: " + ftcRobotPose);
                    Pose petroPose = new Pose(ftcRobotPose.getPosition().x, ftcRobotPose.getPosition().y, ftcRobotPose.getOrientation().getYaw(AngleUnit.RADIANS), FTCCoordinates.INSTANCE);
                    Log.i(LOG_TAG, "petroPose: " + petroPose);
                    telemetry.addData(LOG_TAG, "petroPose: " + petroPose);
                    Pose converted = FTCCoordinates.INSTANCE.convertToPedro(petroPose);
                    Log.i(LOG_TAG, "converted: " + converted);
                    telemetry.addData(LOG_TAG, "converted: " + converted);

                    Position pos = targetPose.getPosition().toUnit(DistanceUnit.INCH);
                    YawPitchRollAngles rot = targetPose.getOrientation();

                    // Camera Coordinate System: Z forward, X right, Y down.
                    // Robot Coordinate System (Pedro): X forward, Y left.
                    // Assuming Camera is mounted forward on the robot.

                    // Transform Camera to Robot Frame
                    // Robot X = Camera Z
                    // Robot Y = -Camera X
                    double r_x = pos.z;
                    double r_y = -pos.x;

                    // Extract Tag Yaw (Rotation around Y-axis in Camera Frame)
                    // YawPitchRollAngles (Z-X-Y) -> Roll is Y-axis
                    // Convert tagYaw to radians for consistent unit usage with tagMapPose.getHeading()
                    double tagYawRadians = Math.toRadians(rot.getRoll(AngleUnit.DEGREES));

                    // Calculate Robot Heading
                    // H_r = H_t - Yaw_{tr}
                    // Since Camera aligns with Robot, Yaw_{tr} = tagYaw.
                    double robotHeading = tagMapPose.getHeading() - tagYawRadians; // Both are now in radians

                    // Calculate Global Position. robotHeading is already in radians.
                    double cosH = Math.cos(robotHeading);
                    double sinH = Math.sin(robotHeading);

                    // P_robot = P_tag - Rotate(H_r) * v_rt
                    // v_rt = (r_x, r_y)
                    // Rotate(H_r) * v_rt = (r_x cosH - r_y sinH, r_x sinH + r_y cosH)
                    double global_dx = r_x * cosH - r_y * sinH;
                    double global_dy = r_x * sinH + r_y * cosH;

                    double cam_x = tagMapPose.getX() - global_dx;
                    double cam_y = tagMapPose.getY() - global_dy;

                    // Adjust for Camera Offset to get Robot Center
                    double offX = CameraOffsets.FRONT_OFFSET_INCHES;
                    double offY = -CameraOffsets.RIGHT_OFFSET_INCHES; // Right is -Y in Robot Frame

                    // Robot Center = Camera Pos - Rotate(H) * Offset
                    double robot_x = cam_x - (offX * cosH - offY * sinH);
                    double robot_y = cam_y - (offX * sinH + offY * cosH);

                    telemetry.update();
                    return new Pose(robot_x, robot_y, robotHeading).getAsCoordinateSystem(PedroCoordinates.INSTANCE);
                }
            }
        }
        telemetry.update();
        return null;
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

    private Optional<FiducialResult> scanObeliskTag() {
        limelight.pipelineSwitch(PIPELINE_ID);
        LLResult result = limelight.getLatestResult();

        if (result != null && result.isValid()) {
            return result.getFiducialResults().stream().filter(fiducialResult -> AprilTagEnum.isValidObeliskTag(fiducialResult.getFiducialId())).findFirst();
        }

        return Optional.empty();
    }


    public void start(){
        limelight.start();
        telemetry.addData(">", "Robot Ready. Press Play");
        telemetry.update();
        // Optional: reduce telemetry update frequency
        telemetry.setMsTransmissionInterval(20);

    }
}