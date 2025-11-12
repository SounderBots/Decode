package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class BluePositions implements RowOnFloorPositions {

    public static double rowStartX = 40;
    public static double rowEndX = 0;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 35;
    public static double secondRowStartY = 58.8;
    public static double thirdRowStartY = 82.5;

    public static double rowEndYOffset = 0;

    public static double headingDegrees = 180;

    public static Pose backShootPosition = new Pose(54, 15, Math.toRadians(115));
    public static Pose frontShootPosition = new Pose(56, 10, Math.toRadians(104));

    public static Pose backStartPosition = new Pose(55.75, 8.16, Math.toRadians(90));
    public static Pose frontStartPosition = new Pose(23.5, 125.75, Math.toRadians(140));

    public static Pose backFinishPosition = new Pose(48, 24, 0);
    public static Pose frontFinishPosition = new Pose(48, 120, 0);

    @Override
    public Pose getFrontShootPosition() {
        return frontShootPosition;
    }

    @Override
    public Pose getBackShootPosition() {
        return backShootPosition;
    }

    //

    @Override
    public Pose getBackStartPosition() {
        return backStartPosition;
    }

    @Override
    public Pose getFrontStartPosition() {
        return frontStartPosition;
    }

    @Override
    public Pose getBackFinishPosition() {
        return backFinishPosition;
    }

    @Override
    public Pose getFrontFinishPosition() {
        return frontFinishPosition;
    }

    //

    @Override
    public Pose getFirstRowStartPosition() {
        return new Pose(rowStartX, firstRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getFirstRowEndPosition() {
        return new Pose(rowEndX, firstRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getSecondRowStartPosition() {
        return new Pose(rowStartX, secondRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getSecondRowEndPosition() {
        return new Pose(rowEndX, secondRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getThirdRowStartPosition() {
        return new Pose(rowStartX, thirdRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getThirdRowEndPosition() {
        return new Pose(rowEndX, thirdRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
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
