package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.arcrobotics.ftclib.command.RunCommand;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.AutonDriveTrain;

@TeleOp
public class AutonDriveTrainCalibrate extends OpModeTemplate {

    AutonDriveTrain autonDriveTrain;

    Telemetry.Line odoXLine;
    Telemetry.Line odoYLine;
    Telemetry.Line thetaLine;
    Telemetry.Line motorLine;
    @Override
    public void initialize() {
        super.initialize();
        autonDriveTrain = new AutonDriveTrain(hardwareMap, telemetry);
        register(autonDriveTrain);
        odoXLine = telemetry.addLine("OdoX");
        odoYLine = telemetry.addLine("OdoY");
        thetaLine = telemetry.addLine("Heading");
        motorLine = telemetry.addLine("Motors");
        schedule(new RunCommand(() -> telemetry.update()));
    }

//    void setupOdoLine() {
//        odoLine.addData("X", () -> autonDriveTrain.getOdoX().getCurrentPosition());
//    }




}
