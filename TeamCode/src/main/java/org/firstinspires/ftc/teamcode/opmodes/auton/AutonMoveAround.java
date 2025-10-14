package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.CommandFactory;

@Autonomous(name="AutonMoveAround", group="Test")
public class AutonMoveAround extends CommandAutoOpMode {


    @Override
    protected Command createCommand() {
        return commandFactory.driveToTargetFieldCentric(-15, 0, 0)
                .alongWith(commandFactory.driveTrainTelemetry());
    }
}
