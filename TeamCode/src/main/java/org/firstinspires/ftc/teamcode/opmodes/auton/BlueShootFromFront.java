package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from front", group="Blue")
@Configurable
public class BlueShootFromFront extends AutonBase {

    // shoot position: x: 8 tooth, y: 11 tooth
    public static double shootingAngle = 144;
    public static Pose startingPosition = new Pose(85, 83, Math.toRadians(shootingAngle));
    public static Pose shootingPosition = new Pose(85, 83, Math.toRadians(shootingAngle));

    public static double shootVelocityScale = 1;

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
}
