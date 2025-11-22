package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.PathType;

@Autonomous(name="Red from back delay shoot", group="Red")
@Configurable
// center to side (8.5 inches)
// center to back (8.3 inches)
// center to front (8.5 inches)
public class RedFromBackDelayShoot extends AutonBase {

    public static long delayInMS = 20000;

    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.LONG;
    }

    @Override
    protected Command createCommand() {
        Pose startPosition = getStartingPosition();
        return commandFactory.sleep(delayInMS).andThen(moveAndShootPreloads()).andThen(intakeRowAndShoot(RowsOnFloor.FIRST, false))
                .andThen(commandFactory.moveTo(startPosition.minus(new Pose(0, -20, getPreloadShootPosition().getHeading())), PathType.LINE));
    }
}
