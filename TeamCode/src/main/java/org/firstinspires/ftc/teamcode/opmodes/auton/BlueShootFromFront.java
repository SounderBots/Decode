package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from front", group="Blue")
@Configurable
public class BlueShootFromFront extends AutonBase {

    // shoot position: x: 8 tooth, y: 11 tooth
//    public static double preloadShootingAngle = 142;
//    public static double rowShootingAngle = 140;
//    public static Pose startingPosition = new Pose(23.5, 125.75, Math.toRadians(preloadShootingAngle));
//    public static Pose preloadShootingPosition = new Pose(64.5, 90, Math.toRadians(preloadShootingAngle));
//    public static Pose rowShootingPosition = new Pose(60, 85, Math.toRadians(rowShootingAngle));

    @Override
    Side getSide() {
        return Side.BLUE;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.SHORT;
    }
}
