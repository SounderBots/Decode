package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Blue shoot from back", group="Blue")
@Configurable
public class BlueShootFromBack extends AutonBase {

    public static Pose startPosition = new Pose(94, 8.16, Math.toRadians(90));
    public static Pose shootingPosition = new Pose(78, 18, Math.toRadians(120));


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
}
