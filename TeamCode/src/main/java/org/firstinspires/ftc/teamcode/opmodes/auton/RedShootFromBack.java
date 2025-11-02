package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red shoot from back", group="Red")
@Configurable
public class RedShootFromBack extends AutonBase {

    public static Pose startPosition = new Pose(50, 10, Math.toRadians(90));
    public static Pose shootingPosition = new Pose(66, 18, Math.toRadians(60));


    @Override
    protected Command createCommand() {
        return shootPreloads() // move to shooting position
            .andThen(intakeRowAndShoot(RowsOnFloor.FIRST)) // shoot first row
            .andThen(intakeRowAndShoot(RowsOnFloor.SECOND)) // shoot second row
//            .andThen(intakeRowAndShoot(RowsOnFloor.THIRD)) // shoot third row
        ;
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
}
