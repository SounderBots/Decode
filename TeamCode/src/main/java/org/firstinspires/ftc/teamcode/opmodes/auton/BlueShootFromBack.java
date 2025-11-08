package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from back", group="Blue")
@Configurable
public class BlueShootFromBack extends AutonBase {

    public static double preloadShootingAngle = 125;
    public static double rowShootingAngle = 110;
    public static Pose startPosition = new Pose(55.75, 8.16, Math.toRadians(90));//new Pose(88.25, 8.16, Math.toRadians(90));
    public static Pose preloadShootingPosition = new Pose(56, 10, Math.toRadians(preloadShootingAngle));//new Pose(86, 11, Math.toRadians(shootingAngle));
    public static Pose rowShootingPosition = new Pose(56, 15, Math.toRadians(rowShootingAngle));//new Pose(86, 11, Math.toRadians(shootingAngle));
    public static double shootVelocityScale = 1.03;

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
        return Side.BLUE;
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
