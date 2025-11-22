package org.firstinspires.ftc.teamcode.subsystems.vision;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.command.CommonConstants;
import org.firstinspires.ftc.teamcode.common.AprilTagEnum;
import org.firstinspires.ftc.teamcode.common.AprilTagPosition;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;

import static com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;

public class LimeLightAlign extends SubsystemBase {

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

    public AprilTagEnum getObeliskAprilTag() {
        FiducialResult fr = scanAprilTag();
        AprilTagEnum aprilTagEnum = null;
        if (fr != null) {
            try {
                aprilTagEnum = AprilTagEnum.fromValue(fr.getFiducialId());
                if (AprilTagEnum.OBELISK_ALL.contains(aprilTagEnum)){
                    telemetry.addData("Scanned Obelisk Tag ID: ", aprilTagEnum.getValue());
                    aprilTagEnum = null;
                } else {
                    telemetry.addData("Not a valid ObelisK Tag ", aprilTagEnum.getValue());
                }
            } catch (Exception e) {
                telemetry.addData("Limelight", "No Valid AprilTags detected");
            }
        } else {
            telemetry.addData("Limelight", "No AprilTags detected");
        }
        telemetry.update();
        return aprilTagEnum;
    }

    public AprilTagPosition getAprilTagPosition(){
        FiducialResult fr = scanAprilTag();
        AprilTagPosition aprilTagPosition = null;
        if (fr != null) {
            try {
                AprilTagEnum aprilTagEnum = null;
                aprilTagEnum = AprilTagEnum.fromValue(fr.getFiducialId());

                double distance = 0d;
                Pose3D pose3D = fr.getTargetPoseCameraSpace();
                Position p = pose3D.getPosition().toUnit(CommonConstants.DISTANCE_UNIT);

                double x = p.x;
                double y = p.y;
                double z = p.z;

                distance = Math.sqrt(x * x + y * y + z * z);

                aprilTagPosition = new AprilTagPosition(aprilTagEnum, distance, fr.getTargetXDegrees(), fr.getTargetYDegrees());

                boolean addTelemetry = MainTeleop.Telemetry.LimeLight;
                if(addTelemetry) {
                    telemetry.addData("Tag ID", aprilTagEnum.getValue());
                    telemetry.addData("x", x);
                    telemetry.addData("y", y);
                    telemetry.addData("z", z);
                    telemetry.addData("Estimated Distance", aprilTagPosition.distance());
                    telemetry.addData("Estimated Horizontal shift", aprilTagPosition.horizontalAngle());
                    telemetry.addData("Estimated Vertical Shift", aprilTagPosition.verticalAngle());
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


    public void start(){
        limelight.start();
        telemetry.addData(">", "Robot Ready. Press Play");
        telemetry.update();
        // Optional: reduce telemetry update frequency
        telemetry.setMsTransmissionInterval(20);

    }
}