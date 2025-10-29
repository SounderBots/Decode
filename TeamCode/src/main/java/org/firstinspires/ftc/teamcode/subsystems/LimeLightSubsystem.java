package org.firstinspires.ftc.teamcode.subsystems;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

import static com.qualcomm.hardware.limelightvision.LLResultTypes.FiducialResult;

public class LimeLightSubsystem extends SubsystemBase {

    private Limelight3A limelight;

    protected Telemetry telemetry;

    private static final int PIPELINE_ID = 4; // April tag pipeline id

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

            Pose3D pose3D = fr.getTargetPoseCameraSpace();
            Position p = pose3D.getPosition().toUnit(DistanceUnit.INCH);

            double x = p.x;
            double y = p.y;
            double z = p.z;

            distance = Math.sqrt(x*x + y*y + z*z);

            telemetry.addData("Tag ID", tagID);
            telemetry.addData("x", x);
            telemetry.addData("y", y);
            telemetry.addData("z", z);
            telemetry.addData("Estimated Distance", "%.2f inches", distance);
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
}