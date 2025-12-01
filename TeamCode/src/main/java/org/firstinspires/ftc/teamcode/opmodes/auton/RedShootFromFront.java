package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;

@Autonomous(name="Red shoot from front", group="Red")
@Configurable
public class RedShootFromFront extends AutonBase {

    // shoot position: x: 8 tooth, y: 11 tooth
//    public static double preloadShootingAngle = 38;
//    public static double rowShootingAngle = 38;
//    public static Pose startingPosition = new Pose(120.5, 125.75, Math.toRadians(preloadShootingAngle));
//    public static Pose preloadShootingPosition = new Pose(79.5, 84.75, Math.toRadians(preloadShootingAngle));
//    public static Pose rowShootingPosition = new Pose(88.5, 93.75, Math.toRadians(rowShootingAngle));

    public static double shootVelocityScale = 1.006;
//
    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.SHORT;
    }
}
