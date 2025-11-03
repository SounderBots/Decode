package org.firstinspires.ftc.teamcode.opmodes.teleop;

import android.util.Log;

import com.arcrobotics.ftclib.command.RunCommand;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.AutonDriveTrain;

@TeleOp
public class AutonDriveTrainCalibrate extends OpModeTemplate {
    private static final String LOG_TAG = AutonDriveTrainCalibrate.class.getSimpleName();

    AutonDriveTrain autonDriveTrain;

    @Override
    public void initialize() {
        super.initialize();
        autonDriveTrain = new AutonDriveTrain(hardwareMap, telemetry);
        register(autonDriveTrain);

        schedule(new RunCommand(() -> {
            setupOdoLine();
//            setupMotors();
            telemetry.update();
        }));
    }

    void setupOdoLine() {
        autonDriveTrain.getPinpoint().update();
        Log.i(LOG_TAG, String.format("Odo: X (inches): %1$f, Y (inches): %2$f, Heading (Degrees): %3$f", autonDriveTrain.getPinpoint().getPosX(DistanceUnit.INCH), autonDriveTrain.getPinpoint().getPosY(DistanceUnit.INCH), autonDriveTrain.getPinpoint().getHeading(AngleUnit.DEGREES)));
        telemetry.addData("Odo", "");
        telemetry.addData("X (inches)", autonDriveTrain.getPinpoint().getPosX(DistanceUnit.INCH));
        telemetry.addData("Y (inches)", autonDriveTrain.getPinpoint().getPosY(DistanceUnit.INCH));
        telemetry.addData("Heading (Degrees)", autonDriveTrain.getPinpoint().getHeading(AngleUnit.DEGREES));
    }

    void setupMotors() {
        telemetry.addData("Motors", "");
        telemetry.addData("Front Left power", autonDriveTrain.getFrontLeft().motor.getPower());
        telemetry.addData("Front Right power", autonDriveTrain.getFrontRight().motor.getPower());
        telemetry.addData("Back Left power", autonDriveTrain.getBackLeft().motor.getPower());
        telemetry.addData("Back Right power", autonDriveTrain.getBackRight().motor.getPower());
    }
}
