package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.PathType;

@Autonomous(name="Blue from back delay shoot", group="Blue")
@Configurable
public class BlueFromBackDelayShoot extends AutonBase {

    public static long delayInMS = 18000;

//    public static double preloadShootingAngle = 104;
//    public static double rowShootingAngle = 104;
//    public static Pose startPosition = new Pose(55.75, 8.16, Math.toRadians(90));//new Pose(88.25, 8.16, Math.toRadians(90));
//    public static Pose preloadShootingPosition = new Pose(56, 10, Math.toRadians(104));//new Pose(86, 11, Math.toRadians(shootingAngle));
//    public static Pose rowShootingPosition = new Pose(54, 15, Math.toRadians(115));//new Pose(86, 11, Math.toRadians(shootingAngle));
//    public static double shootVelocityScale = 1.05;

    @Override
    Side getSide() {
        return Side.BLUE;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.LONG;
    }

    @Override
    protected Command createCommand() {
        Pose startPosition = getStartingPosition();
        return commandFactory.sleep(delayInMS)
                .andThen(moveAndShootPreloads())
                .andThen(intakeRowAndShoot(RowsOnFloor.GPP, false))
                .andThen(commandFactory.moveTo((new Pose(37, 35, 0)), PathType.LINE, 0.7));
    }
}
