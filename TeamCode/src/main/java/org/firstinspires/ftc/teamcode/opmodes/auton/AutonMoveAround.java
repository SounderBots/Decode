package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="AutonMoveAround", group="Test")
public class AutonMoveAround extends CommandAutoOpMode {


    @Override
    protected Command createCommand() {
        return commandFactory
                .startMove(new Pose(20, 25, Math.toRadians(90)))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(new Pose(0, 25, Math.toRadians(180))))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(new Pose(0, 0, Math.toRadians(360))));
    }
}
