package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class RedSideRowsOnFloorPositions {
    // each row y increase 24 inches
    public static Pose firstRowStartingPosition = new Pose(36, 24, Math.toRadians(180));
    public static Pose secondRowStartingPosition = new Pose(36, 48, Math.toRadians(180));
    public static Pose thirdRowStartingPosition = new Pose(36, 72, Math.toRadians(180));

    public static Pose firstRowEndingPosition = new Pose(20, 24, Math.toRadians(180));
    public static Pose secondRowEndingPosition = new Pose(20, 48, Math.toRadians(180));
    public static Pose thirdRowEndingPosition = new Pose(20, 72, Math.toRadians(180));
}
