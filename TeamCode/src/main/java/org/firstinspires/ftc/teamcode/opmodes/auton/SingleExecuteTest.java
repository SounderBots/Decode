package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.SingleExecuteCommand;


@Autonomous(name = "single execute test", group = "Test")
public class SingleExecuteTest extends AutonBase{
    @Override
    Pose getShootingPosition() {
        return null;
    }

    @Override
    Pose getStartingPosition() {
        return null;
    }

    @Override
    Side getSide() {
        return null;
    }

    @Override
    protected ShootMode shootMode() {
        return null;
    }

    @Override
    protected Command createCommand() {
        return new SingleExecuteCommand(commandFactory.getTransferChamber()::TurnOnSlowChamberRoller)
                .andThen(commandFactory.sleep(2000))
                .andThen(new SingleExecuteCommand(commandFactory.getTransferChamber()::TurnOffChamberRoller));
    }
}
