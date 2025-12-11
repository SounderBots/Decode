package org.firstinspires.ftc.teamcode.subsystems.vision;

import static com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.pedropathing.ftc.FTCCoordinates;
import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Supplier;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.command.CommonConstants;
import org.firstinspires.ftc.teamcode.common.AprilTagEnum;
import org.firstinspires.ftc.teamcode.common.AprilTagPosition;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.AutonCommonConfigs;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;

import java.util.Optional;

public class LimeLightAlign extends SubsystemBase {

    private static final String LOG_TAG = LimeLightAlign.class.getSimpleName();

    private Limelight3A limelight;

    protected Telemetry telemetry;

    private static final int PIPELINE_ID = 4; // April tag pipeline id

    private double horizontalAngle, verticalAngle;

    Supplier<Pose> petroPathingPoseSupplier;

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

        if (petroPathingPoseSupplier != null) {
            limelight.updateRobotOrientation(petroPathingPoseSupplier.get()
                    .getAsCoordinateSystem(FTCCoordinates.INSTANCE).getHeading());
        }

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

    public LimeLightAlign withHeadingSupplier(Supplier<Pose> poseSupplier) {
        this.petroPathingPoseSupplier = poseSupplier;
        return this;
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

    public Optional<Pose> getRobotPosition() {
        if (petroPathingPoseSupplier == null) {
            Log.i(LOG_TAG, "petroPathingPoseSupplier is null");
            return Optional.empty();
        }

        limelight.updateRobotOrientation(petroPathingPoseSupplier.get()
                .getAsCoordinateSystem(FTCCoordinates.INSTANCE).getHeading());
        LLResult scanResult = limelight.getLatestResult();

        if (scanResult == null) {
            Log.i(LOG_TAG, "no limelight scan result");
            return Optional.empty();
        }

        Pose3D limelightReported = scanResult.getBotpose();
        if (limelightReported == null) {
            Log.i(LOG_TAG, "no limelight reported pose");
            return Optional.empty();
        }

        Pose ftcPose = new Pose(
                meterToInch(limelightReported.getPosition().x),
                meterToInch(limelightReported.getPosition().y),
                limelightReported.getOrientation().getYaw(AngleUnit.RADIANS),
                FTCCoordinates.INSTANCE);
        Log.i(AutonCommonConfigs.LOG_TAG, "limelight reported ftcPose = " + ftcPose);
        Pose pedroPose = ftcPose.getAsCoordinateSystem(PedroCoordinates.INSTANCE);
        Log.i(AutonCommonConfigs.LOG_TAG, "limelight reported pedroPose = " + pedroPose);
        return Optional.of(pedroPose);
    }

    private double meterToInch(double meter) {
        return meter * 39.3701;
    }
}