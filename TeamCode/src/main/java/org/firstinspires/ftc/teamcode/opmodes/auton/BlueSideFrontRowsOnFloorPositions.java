package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class BlueSideFrontRowsOnFloorPositions implements RowOnFloorPositions {

    public static double rowStartX = 50;
    public static double rowEndX = 21;

    // each row y increase 24 inches
    public static double firstRowStartY = 42;
    public static double secondRowStartY = 66;
    public static double thirdRowStartY = 90;

    public static double rowEndYOffset = -4;

    public static double headingDegrees = 185;

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
