package org.firstinspires.ftc.teamcode.opmodes.auton.positions;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class BlueShortPositions implements Positions {

    public static double rowStartX = 52;
    public static double rowEndXLongIntake = 10;
    public static double rowEndXShortIntake = 16;

    // each row y increase 24 inches
    //first row is the row close to the trangle
    public static double firstRowStartY = 35;
    public static double secondRowStartY = 60.5;
    public static double thirdRowStartY = 87;

    public static double rowEndYOffset = 0;

    public static double headingDegrees = 180;

    public static double backShootAngleInDegrees = 144.09;
    public static Pose backShootPosition = new Pose(56, 11, Math.toRadians(backShootAngleInDegrees));
    public static double frontShootAngleInDegrees = 136;
    public static Pose frontShootPosition = new Pose(57, 93, Math.toRadians(frontShootAngleInDegrees));

    public static Pose backStartPosition = new Pose(55.75, 8.16, Math.toRadians(90));
    public static Pose frontStartPosition = new Pose(23.5, 125.75, Math.toRadians(backShootAngleInDegrees));

    public static Pose backFinishPosition = new Pose(48, 24, 0);
    public static Pose frontFinishPosition = new Pose(48, (secondRowStartY + thirdRowStartY) / 2 + 10, Math.toRadians(0));
    public static double obeliskObserveHeadingInDegrees = 70;
    public static Pose obeliskObservePosition = frontShootPosition.withHeading(Math.toRadians(obeliskObserveHeadingInDegrees));
    public static Pose GPPShootingPosition = new Pose(57, 93, Math.toRadians(frontShootAngleInDegrees));
    public static Pose PGPShootingPosition = new Pose(67, 83, Math.toRadians(frontShootAngleInDegrees));

    public BlueShortPositions() {
        backShootPosition = backShootPosition.setHeading(Math.toRadians(backShootAngleInDegrees));
        frontShootPosition = frontShootPosition.setHeading(Math.toRadians(frontShootAngleInDegrees));
        obeliskObservePosition = obeliskObservePosition.setHeading(Math.toRadians(obeliskObserveHeadingInDegrees));
    }

    @Override
    public Pose getShortShootPosition() {
        return frontShootPosition;
    }

    @Override
    public Pose getObeliskObservePosition() {
        return obeliskObservePosition;
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
    public Pose getGPPStartPosition() {
        return new Pose(rowStartX, firstRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getGPPEndPosition() {
        return new Pose(rowEndXLongIntake, firstRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getPGPStartPosition() {
        return new Pose(rowStartX, secondRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getPGPEndPosition() {
        return new Pose(rowEndXLongIntake, secondRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getPPGStartPosition() {
        return new Pose(rowStartX, thirdRowStartY, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getPPGEndPosition() {
        return new Pose(rowEndXShortIntake, thirdRowStartY + rowEndYOffset, Math.toRadians(headingDegrees));
    }

    @Override
    public Pose getGPPShootPosition() {
        return GPPShootingPosition;
    }

    @Override
    public Pose getPGPShootPosition() {
        return PGPShootingPosition;
    }

    @Override
    public Pose getPPGShootPosition() {
        return getShortShootPosition();
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
