package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class AutonCommonConfigs {
    public static double DrivetrainIntakePower = .3;
    public static double intakeDriveDistance = 15;
    public static double rowDistance = 24;
    // each row y increase 24 inches
    public static Pose blueFirstRowStartingPosition = new Pose(108, 32.16, Math.toRadians(0));
    // each row y increase 24 inches
    public static Pose redFirstRowStartingPosition = new Pose(46, 32.16, Math.toRadians(180));
}
