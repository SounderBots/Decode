package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class BlueLongPositions implements Positions {

    public static double rowStartX = 44;
    public static double rowEndXLongIntake = 6;
    public static double rowEndXShortIntake = 12;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 35;
    public static double secondRowStartY = 63;
    public static double thirdRowStartY = 83;

    public static double rowEndYOffset = 0;

    public static double headingDegrees = 180;

    public static double backShootAngleInDegrees = 110;
    public static Pose backShootPosition = new Pose(56, 11, Math.toRadians(backShootAngleInDegrees));
    public static double frontShootAngleInDegrees = 136;
    public static Pose frontShootPosition = new Pose(57, 93, Math.toRadians(frontShootAngleInDegrees));

    public static Pose backStartPosition = new Pose(55.75, 8.16, Math.toRadians(90));
    public static Pose frontStartPosition = new Pose(23.5, 125.75, Math.toRadians(140));

    public static Pose backFinishPosition = new Pose(48, 24, 0);
    public static Pose frontFinishPosition = new Pose(48, 120, 0);

    public static double driveTrainIntakePowerScale = 1.5;

    public static double longShootPreloadHeadingInDegree = 75;
    public static Pose longShootPreloadPosition = new Pose(88, 11, Math.toRadians(longShootPreloadHeadingInDegree));

    public BlueLongPositions() {
        backShootPosition = backShootPosition.setHeading(Math.toRadians(backShootAngleInDegrees));
        frontShootPosition = frontShootPosition.setHeading(Math.toRadians(frontShootAngleInDegrees));
    }

    @Override
    public Pose getShortShootPosition() {
        return frontShootPosition;
    }

    @Override
    public Pose getLongShootPosition() {
        return backShootPosition;
    }

    //

    @Override
    public Pose getLongStartPosition() {
        return backStartPosition;
    }

    @Override
    public Pose getShortStartPosition() {
        return frontStartPosition;
    }

    @Override
    public Pose getLongFinishPosition() {
        return backFinishPosition;
    }

    @Override
    public Pose getShortFinishPosition() {
        return frontFinishPosition;
    }

    //

    @Override
    public Pose getFirstRowStartPosition() {
        return new Pose(rowStartX, firstRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getFirstRowEndPosition() {
        return new Pose(rowEndXLongIntake, firstRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getSecondRowStartPosition() {
        return new Pose(rowStartX, secondRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getSecondRowEndPosition() {
        return new Pose(rowEndXLongIntake, secondRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getThirdRowStartPosition() {
        return new Pose(rowStartX, thirdRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getThirdRowEndPosition() {
        return new Pose(rowEndXShortIntake, thirdRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public double getDriveTrainIntakePowerScale() {
        return driveTrainIntakePowerScale;
    }

    @Override
    public Pose getLongPreloadShootPosition() {
        return longShootPreloadPosition;
    }
//    // each row y increase 24 inches
//    public static Pose firstRowStartingPosition = new Pose(108, 32.16, Math.toRadians(0));
//    public static Pose secondRowStartingPosition = new Pose(108, 56.16, Math.toRadians(0));
//    public static Pose thirdRowStartingPosition = new Pose(108, 72, Math.toRadians(0));
//
//    public static Pose firstRowEndingPosition = new Pose(118, 32.16, Math.toRadians(0));
//    public static Pose secondRowEndingPosition = new Pose(118, 56.16, Math.toRadians(0));
//    public static Pose thirdRowEndingPosition = new Pose(118, 80.16, Math.toRadians(0));

}
