package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.AutoIntakeCommand;

@Autonomous (name = "Intake test", group = "Test")
@Configurable
public class IntakeTest extends AutonBase {

    public static double moveDistance = 50;
    public static double drivePower = .4;
    public static long intakeStopDelay = 2000;
    @Override
    Pose getPreloadShootingPosition() {
        return null;
    }

    @Override
    Pose getStartingPosition() {
        return RedShootFromBack.startPosition;
    }

    @Override
    Side getSide() {
        return Side.RED;
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
        Pose startPos = getStartingPosition();
        Pose endPos = startPos.copy().withY(startPos.getY() + moveDistance);
        return new ParallelRaceGroup(commandFactory.startMove(startPos, endPos, drivePower), commandFactory.intakeRow());
    }
}
