package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;

@Autonomous(name="Red shoot from back", group="Red")
@Configurable
// center to side (8.5 inches)
// center to back (8.3 inches)
// center to front (8.5 inches)
public class RedShootFromBack extends AutonBase {

    public static double preloadShootingAngle = 76;
    public static double rowShootingAngle = 65;
    //public static Pose startPosition = new Pose(87.5, 8.3, Math.toRadians(90));// new Pose(55.75, 8.16, Math.toRadians(90));
//    public static Pose preloadShootion = RongPosition = RowOnFloorPositions.getBackShootPosition();
    ////    public static Pose rowShootingPositionwOnFloorPositions.getBackShootPosition();

//    public static double shootVelocityScale = 1.03;

    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.LONG;
    }
}
