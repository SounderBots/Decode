package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class RedRowsOnFloorPositions implements RowOnFloorPositions {

    public static double rowStartX = 100;
    public static double rowEndX = 140;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 35;
    public static double secondRowStartY = 58.8;
    public static double thirdRowStartY = 82.5;

    public static double rowEndYOffset = 0;

    public static double headingDegrees = 0;


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
}
