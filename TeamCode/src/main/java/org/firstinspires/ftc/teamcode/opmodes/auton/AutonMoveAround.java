package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="AutonMoveAround", group="Test")
public class AutonMoveAround extends AutonBase {


    @Override
    Pose getPreloadShootingPosition() {
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
    protected Pose getRowShootingPosition() {
        return null;
    }

    @Override
    protected Command createCommand() {
        return commandFactory.startMove(BlueShootFromFront.startingPosition, AutonCommonConfigs.blueFrontFinishPosition);
    }
}
