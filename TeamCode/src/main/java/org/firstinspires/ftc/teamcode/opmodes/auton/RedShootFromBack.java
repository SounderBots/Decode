package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red shoot from back", group="Red")
@Configurable
// center to side (8.5 inches)
// center to back (8.3 inches)
// center to front (8.5 inches)
public class RedShootFromBack extends AutonBase {

    public static double preloadShootingAngle = 55;
    public static double rowShootingAngle = 60;
    public static Pose startPosition = new Pose(87.5, 8.3, Math.toRadians(90));// new Pose(55.75, 8.16, Math.toRadians(90));
    public static Pose preloadShootingPosition = new Pose(88, 10, Math.toRadians(preloadShootingAngle));
    public static Pose rowShootingPosition = new Pose(88, 18, Math.toRadians(rowShootingAngle));

    public static double shootVelocityScale = 1.04;

    @Override
    Pose getPreloadShootingPosition() {
        return preloadShootingPosition;
    }

    @Override

    Pose getStartingPosition() {
        return startPosition;
    }

    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootMode shootMode() {
        return ShootMode.FAR;
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
