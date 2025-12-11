package org.firstinspires.ftc.teamcode.command;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.AprilTagPosition;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.DriveTrainBase;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;
import org.firstinspires.ftc.teamcode.util.SonicPIDFController;

public class AutoAlignToShoot extends CommandBase {

    @Config
    public static class AutoAlignConfig {
        public static double pid_k = -0.025;

        public static double pid_i = 0;

        public static double pid_d = 0;

        public static double minPower = 0.075;

    }

    private final LimeLightAlign limelight;
    private final Telemetry telemetry;

    private final DriveTrainBase drivetrain;

    private final boolean endAfterAlignment;

    private static final double DefaultPointOfInterestOffset = 2.5;

    private static final double DefaultPointOfInterestRange = 3;

    private static final double CommandTimeout = 1500;

    private long CommandStartTime = 0;


    private  final double pointOfInterestOffset;

    private final double pointOfInterestRange;

    SonicPIDFController pid = new SonicPIDFController(AutoAlignConfig.pid_k, AutoAlignConfig.pid_i, AutoAlignConfig.pid_d);

    public AutoAlignToShoot(LimeLightAlign limelight, DriveTrainBase drivetrain, Telemetry telemetry) {
        this(limelight, drivetrain, telemetry, DefaultPointOfInterestOffset, DefaultPointOfInterestRange, true);
    }

    public AutoAlignToShoot(LimeLightAlign limelight, DriveTrainBase drivetrain, Telemetry telemetry, double pointOfInterestOffset, double pointOfInterestRange, boolean endAfterAlignment) {
        this.limelight = limelight;
        this.telemetry = telemetry;
        this.drivetrain = drivetrain;
        this.pointOfInterestOffset = pointOfInterestOffset;
        this.pointOfInterestRange = pointOfInterestRange;
        this.endAfterAlignment = endAfterAlignment;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    long id = 0;
    boolean isAligned = false;

    @Override
    public void execute() {
        super.execute();

        if(CommandStartTime == 0) {
            CommandStartTime = System.currentTimeMillis();
        }

        AprilTagPosition position = limelight.getAprilTagPosition();
        if (position != null) {
            double horizontalAngle = position.horizontalAngle();
            double error = horizontalAngle - this.pointOfInterestOffset;
            this.isAligned = horizontalAngle > this.pointOfInterestOffset - this.pointOfInterestRange && horizontalAngle < this.pointOfInterestOffset + this.pointOfInterestRange;

            double turnPower = pid.calculatePIDAlgorithm(error);

            if(!isAligned) {
                turnPower = Math.max(Math.abs(turnPower), AutoAlignConfig.minPower) * Math.signum(turnPower);
                drivetrain.Turn(turnPower);
            }  else {
                drivetrain.Stop();
            }

            if(MainTeleop.Telemetry.AutoAlign) {
                telemetry.addData("horizontal Angle", horizontalAngle);
                telemetry.addData("is Aligned", isAligned);
                telemetry.addData("error", error);
                telemetry.addData("turn power", turnPower);
            }
        } else {
            if(MainTeleop.Telemetry.AutoAlign) {
                telemetry.addData("April tag found", false);
            }

            // Don't continue if the april tag is no longer visible
            drivetrain.Stop();
            isAligned = true;
        }

        if(MainTeleop.Telemetry.AutoAlign) {
            telemetry.update();
        }
    }

    long startAlignTime = 0;

    @Override
    public boolean isFinished() {
        if(this.endAfterAlignment) {
            if(System.currentTimeMillis() - CommandStartTime > CommandTimeout) {
                // Timed out
                CommandStartTime = 0;
                return true;
            }

            if(isAligned) {
                // has been aligned for some time. Safe to exit. Required to prevent overshoot.
                if(this.startAlignTime == 0) {
                    // First time seeing align
                    this.startAlignTime = System.currentTimeMillis();
                } else
                    if(System.currentTimeMillis() - startAlignTime > 150) {
                        // has been aligned for a while now. Safe to exit
                        CommandStartTime = 0;
                        return true;
                    }

            } else {
                this.startAlignTime = 0;
            }
        }

        return false;
    }
}
