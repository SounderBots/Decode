package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class RedSideRowsOnFloorPositions {
    // each row y increase 24 inches
    public static Pose firstRowStartingPosition = new Pose(36, 36, Math.toRadians(180));
    public static Pose secondRowStartingPosition = new Pose(36, 60, Math.toRadians(180));
    public static Pose thirdRowStartingPosition = new Pose(36, 84, Math.toRadians(180));

    public static Pose firstRowEndingPosition = new Pose(20, 36, Math.toRadians(180));
    public static Pose secondRowEndingPosition = new Pose(20, 60, Math.toRadians(180));
    public static Pose thirdRowEndingPosition = new Pose(20, 84, Math.toRadians(180));
}
