package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from back", group="Blue")
@Configurable
public class BlueShootFromBack extends AutonBase {

    public static double shootingAngle = 137;
    public static Pose startPosition = new Pose(88.25, 8.16, Math.toRadians(90));
    public static Pose shootingPosition = new Pose(86, 11, Math.toRadians(shootingAngle));
    public static double shootVelocityScale = 1.027;


    @Override
    protected Command createCommand() {
        return shootFromBackCommand();
    }

    @Override
    Pose getShootingPosition() {
        return shootingPosition;
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
}
