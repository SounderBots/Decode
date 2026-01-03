package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.PathType;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;

@Autonomous(name = "turretBlueBackMoveOnly", group = "Blue")
@Configurable
public class turretBlueBackMoveOnly extends AutonBase {

    // Configurable tuning values available in the dashboard
    public static double heading = 3.14159265; // radians

    public static double startPositionX = 44;
    public static double startPositionY = 10;
    public static double preloadPosX = 54;
    public static double preloadPosY = 15;

    public static double DRIVE_SPEED = 0.8;
    public static double returnSpeed = 1;

    public static double shootingSpotX = 52;
    public static double shootingSpotY = 23;

    public static double intakeLine3X = 19;
    public static double intakeLine3Y = 34;

    public static double intakeLine2X = 19;
    public static double intakeLine2Y = 57;

    public static double classifierLineX = 19;
    public static double classifierLineY = 64;
    public static double classifierStrafeX = 10;
    public static double classifierStrafeY = 60;

    public static double endParkY = 72;
    public static double endParkX = 29;

    public static long preLoadTime = 2000;
    public static long lineTwoTime = 2000;
    public static long classifierIntakeTime = 3000;
    public static long classifierShootTime = 2000;
    public static long lineOneTime = 2000;

    @Override
    protected Command createCommand() {
        Pose startPosition = new Pose(startPositionX, startPositionY, heading);
        Pose p1 = new Pose(preloadPosX, preloadPosY, heading);

        //line 2
        Pose p6 = new Pose(shootingSpotX, intakeLine2Y, heading);
        Pose p7 = new Pose(intakeLine2X, intakeLine2Y, heading);
        Pose p8 = new Pose(shootingSpotX, intakeLine2Y, heading);
        Pose p9 = new Pose(shootingSpotX, shootingSpotY, heading);

        //line 3
        Pose p2 = new Pose(shootingSpotX, intakeLine3Y, heading);
        Pose p3 = new Pose(intakeLine3X, intakeLine3Y, heading);
        Pose p4 = new Pose(shootingSpotX, intakeLine3Y, heading);
        Pose p5 = new Pose(shootingSpotX, shootingSpotY, heading);

        //clear classifier
        Pose p10 = new Pose(shootingSpotX, classifierLineY, heading);
        Pose p11 = new Pose(classifierLineX, classifierLineY, heading);
        Pose intakeFromClassifier = new Pose(classifierStrafeX, classifierStrafeY, 2.35619449);
        Pose p12 = new Pose(shootingSpotX, classifierLineY, heading);
        Pose p13 = new Pose(shootingSpotX, shootingSpotY, heading);

        //park at end
        Pose p14 = new Pose(endParkX, endParkY, 0);

        double driveSpeed = DRIVE_SPEED;

        return commandFactory
                //set starting pose and wait
                .startMove(startPosition, p1, PathType.LINE, driveSpeed)
                .andThen(commandFactory.sleep(preLoadTime))

                //intake and shoot second line
                .andThen(commandFactory.moveTo(p6, PathType.LINE, driveSpeed))
                .andThen(commandFactory.startIntake())
                .andThen(commandFactory.moveTo(p7, PathType.LINE, driveSpeed))
                .andThen(commandFactory.stopIntake())
                .andThen(commandFactory.moveTo(p8, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(p9, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(lineTwoTime))

                //clear and take from classifier
                .andThen(commandFactory.moveTo(p10, PathType.LINE, driveSpeed))
                .andThen(commandFactory.startIntake())
                .andThen(commandFactory.moveTo(p11, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(intakeFromClassifier, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(classifierIntakeTime))
                .andThen(commandFactory.stopIntake())
                .andThen(commandFactory.moveTo(p12, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(p13, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(classifierShootTime))

                //intake and shoot third line
                .andThen(commandFactory.moveTo(p2, PathType.LINE, driveSpeed))
                .andThen(commandFactory.startIntake())
                .andThen(commandFactory.moveTo(p3, PathType.LINE, driveSpeed))
                .andThen(commandFactory.stopIntake())
                .andThen(commandFactory.moveTo(p4, PathType.LINE, driveSpeed))
                .andThen(commandFactory.moveTo(p5, PathType.LINE, driveSpeed))
                .andThen(commandFactory.sleep(lineOneTime))

                //park in front of classifier
                .andThen(commandFactory.moveTo(p14, PathType.LINE, returnSpeed));
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