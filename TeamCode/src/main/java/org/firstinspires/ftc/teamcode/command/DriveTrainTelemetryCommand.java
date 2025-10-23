package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.arcrobotics.ftclib.command.RunCommand;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.subsystems.AutonDriveTrain;

public class DriveTrainTelemetryCommand extends RunCommand {
    private static final String LOG_TAG = DriveTrainTelemetryCommand.class.getSimpleName();

    public DriveTrainTelemetryCommand(AutonDriveTrain autonDriveTrain, Telemetry telemetry) {
        super(() ->{
            autonDriveTrain.getOdo().update();
//            Log.i(LOG_TAG, String.format("Odo: X (inches): %1$f, Y (inches): %2$f, Heading (Degrees): %3$f", autonDriveTrain.getOdo().getPosX(DistanceUnit.INCH), autonDriveTrain.getOdo().getPosY(DistanceUnit.INCH), autonDriveTrain.getOdo().getHeading(AngleUnit.DEGREES)));
            telemetry.addData("Odo", "");
            telemetry.addData("X (inches)", autonDriveTrain.getOdo().getPosX(DistanceUnit.INCH));
            telemetry.addData("Y (inches)", autonDriveTrain.getOdo().getPosY(DistanceUnit.INCH));
            telemetry.addData("Heading (Degrees)", autonDriveTrain.getOdo().getHeading(AngleUnit.DEGREES));

            telemetry.addLine();
            telemetry.addData("Motors", "");
            telemetry.addData("Front Left power", autonDriveTrain.getFrontLeft().motor.getPower());
            telemetry.addData("Front Right power", autonDriveTrain.getFrontRight().motor.getPower());
            telemetry.addData("Back Left power", autonDriveTrain.getBackLeft().motor.getPower());
            telemetry.addData("Back Right power", autonDriveTrain.getBackRight().motor.getPower());
            telemetry.update();
        });
    }

    private void generateTelemetry(AutonDriveTrain autonDriveTrain) {

    }
}
