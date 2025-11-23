package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue from back no shoot", group="Blue")
@Configurable
public class BlueFromBackNoShoot extends AutonBase {

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
        Pose endPosition = new Pose(45, 35, 0);
        return commandFactory.sleep(27000)
                .andThen(commandFactory.startMove(startPosition, endPosition));
    }
}
