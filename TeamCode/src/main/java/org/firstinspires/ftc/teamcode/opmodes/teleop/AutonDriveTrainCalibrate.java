package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.arcrobotics.ftclib.command.RunCommand;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.AutonDriveTrain;

@TeleOp
public class AutonDriveTrainCalibrate extends OpModeTemplate {

    AutonDriveTrain autonDriveTrain;

    @Override
    public void initialize() {
        super.initialize();
        autonDriveTrain = new AutonDriveTrain(hardwareMap, telemetry);
        register(autonDriveTrain);

        schedule(new RunCommand(() -> {
            setupOdoLine();
            setupMotors();
            telemetry.update();
        }));
    }

    void setupOdoLine() {
        telemetry.addLine("Odo")
                .addData("X (inches)", () -> autonDriveTrain.getOdo().getPosX(DistanceUnit.INCH))
                .addData("Y (inches)", () -> autonDriveTrain.getOdo().getPosY(DistanceUnit.INCH))
                .addData("Heading (Degrees)", () -> autonDriveTrain.getOdo().getHeading(AngleUnit.DEGREES));
    }

    void setupMotors() {
        telemetry.addLine("Motors")
                .addData("Front Left power", () -> autonDriveTrain.getFrontLeft().motor.getPower())
                .addData("Front Right power", () -> autonDriveTrain.getFrontRight().motor.getPower())
                .addData("Back Left power", () -> autonDriveTrain.getBackLeft().motor.getPower())
                .addData("Back Right power", () -> autonDriveTrain.getBackRight().motor.getPower());
    }
}
