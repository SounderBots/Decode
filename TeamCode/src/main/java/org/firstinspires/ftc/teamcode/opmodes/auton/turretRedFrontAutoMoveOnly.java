package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.PathType;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;

@Autonomous(name = "turretRedFrontAutoMoveOnly", group = "Red")
@Configurable
public class turretRedFrontAutoMoveOnly extends AutonBase {

    // Configurable tuning values available in the dashboard
    public static double heading = 0; // radians

    public static double startPositionX = 128.0;
    public static double startPositionY = 112.5;

    public static double DRIVE_SPEED = 0.6;

    public static double shootingSpotX = 100;
    public static double shootingSpotY = 95;

    public static double intakeLine1X = 125;
    public static double intakeLine1Y = 84;

    public static double intakeLine2X = 120;
    public static double intakeLine2Y = 60;

    public static double classifierLineX = 128;
    public static double classifierLineY = 63;
    public static double classifierStrafe = 61;

    public static double endParkY = 72;
    public static double endParkX = 115;

    public static long preLoadTime = 2000;
    public static long lineTwoTime = 2000;
    public static long classifierIntakeTime = 3000;
    public static long classifierShootTime = 2000;
    public static long lineOneTime = 2000;

    @Override
    protected Command createCommand() {
        Pose startPosition = new Pose(startPositionX, startPositionY, heading);
        Pose p1 = new Pose(shootingSpotX, shootingSpotY, heading);

        //line 1
        Pose p2 = new Pose(shootingSpotX, intakeLine1Y, heading);
        Pose p3 = new Pose(intakeLine1X, intakeLine1Y, heading);
        Pose p4 = new Pose(shootingSpotX, intakeLine1Y, heading);
        Pose p5 = new Pose(shootingSpotX, shootingSpotY, heading);

        //line 2
        Pose p6 = new Pose(shootingSpotX, intakeLine2Y, heading);
        Pose p7 = new Pose(intakeLine2X, intakeLine2Y, heading);
        Pose p8 = new Pose(shootingSpotX, intakeLine2Y, heading);
        Pose p9 = new Pose(shootingSpotX, shootingSpotY, heading);

        //clear classifier
        Pose p10 = new Pose(shootingSpotX, classifierLineY, heading);
        Pose p11 = new Pose(classifierLineX, classifierLineY, heading);
        Pose rotate45 = new Pose(classifierLineX, classifierLineY, 0.78539816);
        Pose strafeRight2 = new Pose(classifierLineX, classifierStrafe, 0.78539816);
        Pose p12 = new Pose(shootingSpotX, classifierLineY, heading);
        Pose p13 = new Pose(shootingSpotX, shootingSpotY, heading);

        //park at end
        Pose p14 = new Pose(endParkX, endParkY, 3.14159265);

        double driveSpeed = DRIVE_SPEED;

        return commandFactory
                //shoot preload
                .startMove(startPosition, p1, PathType.LINE, driveSpeed)
                .andThen(commandFactory.sleep(preLoadTime))

                //intake and shoot second line
                .andThen(commandFactory.moveTo(p6, PathType.LINE, driveSpeed))
                .andThen(commandFactory.startIntake())
                .andThen(commandFactory.moveTo(p7, PathType.LINE, driveSpeed))
                .andThen(commandFactory.stopIntake())
                .andThen(commandFactory.moveTo(p8, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(p13, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(lineTwoTime))

                //clear and take from classifier
                .andThen(commandFactory.moveTo(p10, PathType.LINE, driveSpeed))
                .andThen(commandFactory.startIntake())
                .andThen(commandFactory.moveTo(p11, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(rotate45, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(strafeRight2, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(classifierIntakeTime))
                .andThen(commandFactory.stopIntake())
                .andThen(commandFactory.moveTo(p12, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(p13, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(classifierShootTime))

                //intake and shoot first line
                .andThen(commandFactory.moveTo(p2, PathType.LINE, driveSpeed))
                .andThen(commandFactory.startIntake())
                .andThen(commandFactory.moveTo(p3, PathType.LINE, driveSpeed))
                .andThen(commandFactory.stopIntake())
                .andThen(commandFactory.moveTo(p4, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(p5, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(lineOneTime))

                //park in front of classifier
                .andThen(commandFactory.moveTo(p14, PathType.LINE, driveSpeed));
    }

    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.SHORT;
    }
}