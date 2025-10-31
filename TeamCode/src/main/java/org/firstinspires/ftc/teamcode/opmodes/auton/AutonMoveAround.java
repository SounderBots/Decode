package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="AutonMoveAround", group="Test")
public class AutonMoveAround extends CommandAutoOpMode {


    @Override
    protected Command createCommand() {
        return commandFactory
                .startMove(new Pose(10, 15, 0))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(new Pose(0, 15, 0)))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(new Pose(0, 0, 0)));
    }
}
