package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.AutonDriveTrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.scoring.SingleShooter;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandFactory {

    final Telemetry telemetry;
    final AutonDriveTrain autonDriveTrain;
    final Follower follower;
    final TeleopDrivetrain teleopDrivetrain;
    final Intake intake;
    final SingleShooter shooter;


//    public Command driveToTarget(DriveToTargetCommand.DriveParameters driveParameters) {
//        return new DriveToTargetCommand(autonDriveTrain, telemetry, driveParameters);
//    }
//
//    public Command driveToTargetFieldCentric(double targetXInches, double targetYInches, double targetHeadingInDegrees) {
//        return new MecanumMoveToTargetCommand(autonDriveTrain.getMecanumDrive(), autonDriveTrain.getPinpoint(), new Pose2d(targetXInches, targetYInches, new Rotation2d(Math.toRadians(targetHeadingInDegrees))), 2000);
//    }

    public Command sleep(long durationMs) {
        return new WaitCommand(durationMs);
    }

//    public Command driveTrainTelemetry() {
//        return new DriveTrainTelemetryCommand(autonDriveTrain, telemetry);
//    }

    public Command startMove(Pose end) {
        return startMove(new Pose(0, 0, 0), end);
    }

    public Command startMove(Pose start, Pose end) {
        return new DriveToTargetPedroPathCommand(follower, start, end, true);
    }

    public Command moveTo(Pose end) {
        return new DriveToTargetPedroPathCommand(follower, end, false);
    }

    /**
     * Prepare for intake, like start roller etc.
     */
    public Command prepareIntake() {
        return sleep(200);
    }

    public Command intake() {
        return sleep(1000);
    }

    public Command shoot() {
        return sleep(1000);
    }
}
