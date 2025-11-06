package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "Shoot Test", group = "Test")
public class ShootTest extends AutonBase{

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
        return commandFactory.loadAndShoot(commandFactory.closeShoot());
    }
}
