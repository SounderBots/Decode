package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public class AutonCommonConfigs {
    public static double DrivetrainIntakePower = .35; //The speed the robot moves when intaking, not intake wheel speed
    public static double backShootVelocityScale = 1.03;
    public static double frontShootVelocityScale = 1.006;

    public static long betweenShootDelays = 400;

//    public static Pose blueFrontFinishPosition = new Pose(48, 130, 0);
//    public static Pose redFrontFinishPosition = new Pose(96, 120, 0);
//
//    public static Pose blueBackFinishPosition = new Pose(48, 24, 0);
//    public static Pose redBackFinishPosition = new Pose(96, 24, 0);
}
