package org.firstinspires.ftc.teamcode.opmodes.auton.constants;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class AutonCommonConfigs {
    public static final String LOG_TAG = "AUTO_DEBUG";
    public static double DrivetrainIntakePower = .6; //The speed the robot moves when intaking, not intake wheel speed
    public static double backShootVelocityScale = 1.2;
    public static double frontShootVelocityScale = .9;

    public static long betweenShootDelays = 400;

    public static double TiltServoHi = .85;
    public static double TiltServoLo = 1;

    public static double slowMoveSpeed = .6;
    public static double middleMoveSpeed = .7;
    public static double fastMoveSpeed = .7;

    public static long shootWithLoadTimeoutInMS = 3750;
    public static long shootWithoutLoadTimeoutInMS = 3500;

//    public static Pose blueFrontFinishPosition = new Pose(48, 130, 0);
//    public static Pose redFrontFinishPosition = new Pose(96, 120, 0);
//
//    public static Pose blueBackFinishPosition = new Pose(48, 24, 0);
//    public static Pose redBackFinishPosition = new Pose(96, 24, 0);
}
