package org.firstinspires.ftc.teamcode.opmodes.auton.test;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.CommandRuntimeSharedProperties;
import org.firstinspires.ftc.teamcode.command.SingleExecuteCommand;
import org.firstinspires.ftc.teamcode.opmodes.auton.CommandAutoOpMode;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;

@Autonomous(name="End with command test", group="Test")
@Configurable
public class EndWithTest extends CommandAutoOpMode {
    @Override
    protected Command createCommand() {
        return commandFactory.startMove(new Pose(0, 0, 0), new Pose(30, 0, 0))
                .andThen(new SingleExecuteCommand(() -> {
                    CommandRuntimeSharedProperties.observedObeliskShowedRow = RowsOnFloor.PGP;
                }));
    }
}
