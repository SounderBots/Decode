package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class AutonCommonConfigs {
    public static double DrivetrainIntakePower = .30; //The speed the robot moves when intaking, not intake wheel speed
    public static double backShootVelocityScale = 1.1;
    public static double frontShootVelocityScale = 1.007;

    public static long betweenShootDelays = 400;

    public static double TiltServoHi = 1.25;
    public static double TiltServoLo = 1.5;

    public static double slowMoveSpeed = .65;
    public static double middleMoveSpeed = .7;
    public static double fastMoveSpeed = .8;

    public static long shootWithLoadTimeoutInMS = 5000;
    public static long shootWithoutLoadTimeoutInMS = 4000;

//    public static Pose blueFrontFinishPosition = new Pose(48, 130, 0);
//    public static Pose redFrontFinishPosition = new Pose(96, 120, 0);
//
//    public static Pose blueBackFinishPosition = new Pose(48, 24, 0);
//    public static Pose redBackFinishPosition = new Pose(96, 24, 0);
}
