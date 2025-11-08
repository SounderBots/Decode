package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class AutonCommonConfigs {
    public static double DrivetrainIntakePower = .3;
    public static double intakeDriveDistance = 15;
    public static double rowDistance = 24;

    public static long betweenShootDelays = 400;

    public static Pose blueFinishPosition = new Pose(48, 36, 0);
    public static Pose redFinishPosition = new Pose(96, 36, 0);
}
