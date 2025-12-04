package org.firstinspires.ftc.teamcode.opmodes.auton.positions;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class RedShortPositions implements Positions {

    public static double rowStartX = 90;
    public static double rowEndXLongIntake = 146;
    public static double rowEndXShortIntake = 132;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 33;
    public static double secondRowStartY = 61;
    public static double thirdRowStartY = 82.5;

    public static double intakeHeadingDegrees = 0;

    public static double backShootAngleInDegrees = 70;
    public static Pose backShootPosition = new Pose(88, 11, Math.toRadians(backShootAngleInDegrees));

    @Override
    public Pose getShortPreloadShootPosition() {
        return getShortShootPosition().withHeading(Math.toRadians(preloadShootAngleInDegree));
    }

    public static double frontShootAngleInDegrees = 38;
    public static Pose frontShootPosition = new Pose(87, 93, Math.toRadians(frontShootAngleInDegrees));

    public static Pose backStartPosition = new Pose(87.5, 8.3, Math.toRadians(90));
    public static Pose frontStartPosition = new Pose(120.5, 125.75, Math.toRadians(38));

    public static Pose backFinishPosition = new Pose(96, 24, 0);
    public static Pose frontFinishPosition = new Pose(96, 120, 0);

    public static double preloadShootAngleInDegree = 42;

    public static double obeliskObserveHeadingInDegrees = 110;
    public static Pose obeliskObservePosition = frontShootPosition.withHeading(Math.toRadians(obeliskObserveHeadingInDegrees));

    public RedShortPositions() {
        backShootPosition = backShootPosition.setHeading(Math.toRadians(backShootAngleInDegrees));
        frontShootPosition = frontShootPosition.setHeading(Math.toRadians(frontShootAngleInDegrees));
        obeliskObservePosition = obeliskObservePosition.setHeading(Math.toRadians(obeliskObserveHeadingInDegrees));
    }

    @Override
    public Pose getLongShootPosition() {
        return backShootPosition;
    }

    public Pose getShortShootPosition() {
        return frontShootPosition;
    }

    @Override
    public Pose getObeliskObservePosition() {
        return obeliskObservePosition;
    }

    @Override
    public Pose getShortStartPosition() {
        return frontStartPosition;
    }

    public Pose getLongStartPosition() {
        return backStartPosition;
    }

    @Override
    public Pose getShortFinishPosition() {
        return frontFinishPosition;
    }

    @Override
    public Pose getLongFinishPosition() {
        return backFinishPosition;
    }


    //

    @Override
    public Pose getGPPStartPosition() {
        return new Pose(rowStartX, firstRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getGPPEndPosition() {
        return new Pose(rowEndXLongIntake, firstRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getPGPStartPosition() {
        return new Pose(rowStartX, secondRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getPGPEndPosition() {
        return new Pose(rowEndXLongIntake, secondRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getPPGStartPosition() {
        return new Pose(rowStartX, thirdRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getPPGEndPosition() {
        return new Pose(rowEndXShortIntake, thirdRowStartY, Math.toRadians(intakeHeadingDegrees));
    }
}
