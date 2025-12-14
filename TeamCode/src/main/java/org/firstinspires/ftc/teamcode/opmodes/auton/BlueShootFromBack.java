package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;

@Autonomous(name="Blue shoot from back", group="Blue")
@Configurable
public class BlueShootFromBack extends AutonBase {

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
    boolean isDriveConsiderStopError() {
        return true;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.LONG;
    }
}
