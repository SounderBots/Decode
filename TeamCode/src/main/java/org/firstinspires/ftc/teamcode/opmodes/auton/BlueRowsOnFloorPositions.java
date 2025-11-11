package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class BlueRowsOnFloorPositions implements RowOnFloorPositions {

    public static double rowStartX = 40;
    public static double rowEndX = 0;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 35.1;
    public static double secondRowStartY = 59.1;
    public static double thirdRowStartY = 83.1;

    public static double rowEndYOffset = 0;

    public static double headingDegrees = 180;

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
