package org.firstinspires.ftc.teamcode.pedroPathing;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Configurable
public class Constants {
    public static class RobotMain {
        public static FollowerConstants followerConstants = new FollowerConstants()
                .mass(10)
                .forwardZeroPowerAcceleration(-30.01)
                .lateralZeroPowerAcceleration(-74.366)
                .translationalPIDFCoefficients(new PIDFCoefficients(
                        0.08,
                        0,
                        0.01,
                        0.025
                ))
                .secondaryTranslationalPIDFCoefficients(new PIDFCoefficients(
                        0.15,
                        0,
                        0.005,
                        0.0006
                ))
                .translationalPIDFSwitch(4)
                .headingPIDFCoefficients(new PIDFCoefficients(
                        1.2,
                        0,
                        0.0001,
                        0.03
                ))
                .secondaryHeadingPIDFCoefficients(new PIDFCoefficients(
                        1,
                        0,
                        0.1,
                        0.0005
                ))
                .drivePIDFCoefficients(new FilteredPIDFCoefficients(
                        0.0675,
                        0,
                        0.005,
                        0.6,
                        0.015
                ))
                .secondaryDrivePIDFCoefficients(new FilteredPIDFCoefficients(
                        0.025,
                        0,
                        0.00007,
                        0.6,
                        0.01
                ))
                .drivePIDFSwitch(15)
                .centripetalScaling(0.0006);

        public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1.2, 1.8);

        public static MecanumConstants driveConstants = new MecanumConstants()
                .maxPower(.8)
                .rightFrontMotorName("FR")
                .rightRearMotorName("BR")
                .leftRearMotorName("BL")
                .leftFrontMotorName("FL")
                .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
                .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
                .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
                .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
                .xVelocity(81.32)
                .yVelocity(72.9280);

        public static MecanumConstants intakeDriveConstants = new MecanumConstants()
                .maxPower(.5)
                .rightFrontMotorName("FR")
                .rightRearMotorName("BR")
                .leftRearMotorName("BL")
                .leftFrontMotorName("FL")
                .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
                .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
                .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
                .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
                .xVelocity(81.32)
                .yVelocity(72.9280);

        // axis distance: 11.375 inch (x direction)
        // axis length: 15.5 inch (y direction, left wheel center to right wheel center)
        // forward pod is 2 inch away from right side wheel center, so it is 15.5 / 2 - 2 = 5.75 off
        // strafe pod is 2 inch front of back axis, so it is 11.375 / 2 - 2 = 3.6875 inch off
        // according to this page: https://pedropathing.com/docs/pathing/tuning/localization/pinpoint
        // Mecanum Wheel radius is 52mm or 2.472 inch
        //
        public static PinpointConstants localizerConstants = new PinpointConstants()
                .forwardPodY(5.75)
                .strafePodX(-3.6875)
                .distanceUnit(DistanceUnit.INCH)
                .hardwareMapName("pinpoint")
                .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
                .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
                .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD);

    }
    public static class Backup {
        public static FollowerConstants followerConstants = new FollowerConstants()
                .mass(5.2)
                .forwardZeroPowerAcceleration(-58.69)
                .lateralZeroPowerAcceleration(-85.0)
                .translationalPIDFCoefficients(new PIDFCoefficients(
                        0.03,
                        0,
                        0,
                        0.023
                ))
                .translationalPIDFSwitch(4)
                .headingPIDFCoefficients(new PIDFCoefficients(
                        0.5,
                        0,
                        0.005,
                        0.018
                ))
                .drivePIDFCoefficients(new FilteredPIDFCoefficients(
                        0.1,
                        0,
                        0.0005,
                        0.6,
                        0.015
                ))
                .drivePIDFSwitch(15)
                .centripetalScaling(0.00065);

        public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1, 1);

        public static MecanumConstants driveConstants = new MecanumConstants()
                .maxPower(1)
                .rightFrontMotorName("FR")
                .rightRearMotorName("BR")
                .leftRearMotorName("BL")
                .leftFrontMotorName("FL")
                .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
                .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
                .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
                .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
                .xVelocity(93.1377)
                .yVelocity(72.9280);

        public static PinpointConstants localizerConstants = new PinpointConstants()
                .forwardPodY(-5.75)
                .strafePodX(-6)
                .distanceUnit(DistanceUnit.INCH)
                .hardwareMapName("pinpoint")
                .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD)
                .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
                .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED);

    }

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(RobotMain.followerConstants, hardwareMap)
                .pathConstraints(RobotMain.pathConstraints)
                .mecanumDrivetrain(RobotMain.driveConstants)
                .pinpointLocalizer(RobotMain.localizerConstants)
                .build();
    }
}