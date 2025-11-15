package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class RedPositions implements Positions {

    public static double rowStartX = 100;
    public static double rowEndX = 138;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 33;
    public static double secondRowStartY = 58;
    public static double thirdRowStartY = 82.5;

    public static double intakeHeadingDegrees = 0;

    public static double backShootAngleInDegrees = 70;
    public static Pose backShootPosition = new Pose(88, 11, Math.toRadians(backShootAngleInDegrees));
    public static double frontShootAngleInDegrees = 38;
    public static Pose frontShootPosition = new Pose(79.5, 84.75, Math.toRadians(frontShootAngleInDegrees));

    public static Pose backStartPosition = new Pose(87.5, 8.3, Math.toRadians(90));
    public static Pose frontStartPosition = new Pose(120.5, 125.75, Math.toRadians(38));

    public static Pose backFinishPosition = new Pose(96, 24, 0);
    public static Pose frontFinishPosition = new Pose(96, 120, 0);

    public RedPositions() {
        backShootPosition = backShootPosition.setHeading(Math.toRadians(backShootAngleInDegrees));
        frontShootPosition = frontShootPosition.setHeading(Math.toRadians(frontShootAngleInDegrees));
    }

    @Override
    public Pose getLongShootPosition() {
        return backShootPosition;
    }

    public Pose getShortShootPosition() {
        return frontShootPosition;
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
    public Pose getFirstRowStartPosition() {
        return new Pose(rowStartX, firstRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getFirstRowEndPosition() {
        return new Pose(rowEndX, firstRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getSecondRowStartPosition() {
        return new Pose(rowStartX, secondRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getSecondRowEndPosition() {
        return new Pose(rowEndX, secondRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getThirdRowStartPosition() {
        return new Pose(rowStartX, thirdRowStartY, Math.toRadians(intakeHeadingDegrees));
    }

    @Override
    public Pose getThirdRowEndPosition() {
        return new Pose(rowEndX, thirdRowStartY, Math.toRadians(intakeHeadingDegrees));
    }
}
