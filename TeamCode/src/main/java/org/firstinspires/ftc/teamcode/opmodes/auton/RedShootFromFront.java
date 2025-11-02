package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red shoot from front", group="Red")
public class RedShootFromFront extends AutonBase {

    public static Pose startingPosition = new Pose(60, 60, Math.toRadians(0));
    public static Pose shootingPosition = new Pose(66, 18, Math.toRadians(60));

    @Override
    protected Command createCommand() {
        return shootPreloads()
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND))
//                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST))
        ;
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
}
