package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from front", group="Blue")
@Configurable
public class BlueShootFromFront extends AutonBase {

    public static Pose startingPosition = new Pose(84, 60, Math.toRadians(180));
    public static Pose shootingPosition = new Pose(78, 18, Math.toRadians(150));

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
}
