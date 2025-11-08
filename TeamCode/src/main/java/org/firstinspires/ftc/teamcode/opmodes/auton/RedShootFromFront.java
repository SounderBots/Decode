package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red shoot from front", group="Red")
@Configurable
public class RedShootFromFront extends AutonBase {

    // shoot position: x: 8 tooth, y: 11 tooth
    public static double shootingAngle = 36;
    public static Pose startingPosition = new Pose(59, 83, Math.toRadians(shootingAngle));
    public static Pose shootingPosition = new Pose(59, 83, Math.toRadians(shootingAngle));

    public static double shootVelocityScale = 1.01;

    @Override
    protected Command createCommand() {
        return shootFromFrontCommand();
    }

    @Override
    Pose getShootingPosition() {
        return shootingPosition;
    }

    @Override
    Pose getStartingPosition() {
        return startingPosition;
    }

    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootMode shootMode() {
        return ShootMode.CLOSE;
    }

    @Override
    protected double getShootVelocityScale() {
        return shootVelocityScale;
    }
}
