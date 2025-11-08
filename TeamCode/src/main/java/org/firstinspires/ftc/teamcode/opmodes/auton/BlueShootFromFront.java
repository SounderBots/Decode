package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from front", group="Blue")
@Configurable
public class BlueShootFromFront extends AutonBase {

    // shoot position: x: 8 tooth, y: 11 tooth
    public static double preloadShootingAngle = 142;
    public static double rowShootingAngle = 135;
    public static Pose startingPosition = new Pose(23.5, 125.75, Math.toRadians(preloadShootingAngle));
    public static Pose preloadShootingPosition = new Pose(64.5, 90, Math.toRadians(preloadShootingAngle));
    public static Pose rowShootingPosition = new Pose(60, 85, Math.toRadians(rowShootingAngle));

    public static double shootVelocityScale = 1.008;

    @Override
    Pose getPreloadShootingPosition() {
        return preloadShootingPosition;
    }

    @Override
    Pose getStartingPosition() {
        return startingPosition;
    }

    @Override
    Side getSide() {
        return Side.BLUE;
    }

    @Override
    protected ShootMode shootMode() {
        return ShootMode.CLOSE;
    }

    @Override
    protected double getShootVelocityScale() {
        return shootVelocityScale;
    }

    @Override
    protected Pose getRowShootingPosition() {
        return rowShootingPosition;
    }
}
