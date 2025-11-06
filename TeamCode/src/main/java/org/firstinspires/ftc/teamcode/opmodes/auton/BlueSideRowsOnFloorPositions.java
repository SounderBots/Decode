package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class BlueSideRowsOnFloorPositions {
    // each row y increase 24 inches
    public static Pose firstRowStartingPosition = new Pose(108, 32.16, Math.toRadians(0));
    public static Pose secondRowStartingPosition = new Pose(108, 56.16, Math.toRadians(0));
    public static Pose thirdRowStartingPosition = new Pose(108, 72, Math.toRadians(0));

    public static Pose firstRowEndingPosition = new Pose(118, 32.16, Math.toRadians(0));
    public static Pose secondRowEndingPosition = new Pose(118, 56.16, Math.toRadians(0));
    public static Pose thirdRowEndingPosition = new Pose(118, 80.16, Math.toRadians(0));
}
