package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red shoot from back", group="Red")
@Configurable
public class RedShootFromBack extends AutonBase {

    public static double shootingAngle = 54;
    public static Pose startPosition = new Pose(55.75, 8.16, Math.toRadians(90));
    public static Pose shootingPosition = new Pose(56, 18, Math.toRadians(shootingAngle));


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
        return Side.RED;
    }

    @Override
    protected ShootMode shootMode() {
        return ShootMode.FAR;
    }
}
