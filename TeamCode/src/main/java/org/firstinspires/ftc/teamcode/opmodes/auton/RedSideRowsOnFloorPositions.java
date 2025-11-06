package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class RedSideRowsOnFloorPositions {
    // each row y increase 24 inches
    public static Pose firstRowStartingPosition = new Pose(46, 32.16, Math.toRadians(180));
    public static Pose secondRowStartingPosition = new Pose(42, 56.16, Math.toRadians(180));
    public static Pose thirdRowStartingPosition = new Pose(42, 80.16, Math.toRadians(180));

    public static Pose firstRowEndingPosition = new Pose(26, 32.16, Math.toRadians(180));
    public static Pose secondRowEndingPosition = new Pose(26, 56.16, Math.toRadians(180));
    public static Pose thirdRowEndingPosition = new Pose(26, 80.16, Math.toRadians(180));
}
