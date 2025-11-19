package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.auton.AutonCommonConfigs;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.AutonDriveTrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandFactory {

    public static final int DEFAULT_TIME_OUT = 2000;
    final Telemetry telemetry;
    final AutonDriveTrain autonDriveTrain;
    final Follower follower;
    final TeleopDrivetrain teleopDrivetrain;

    @Getter
    final Intake intake;

    @Getter
    final Shooter shooter;

    @Getter
    final TransferChamber transferChamber;
//    final Shooter shooter;


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
    public Command startMove(Pose start, Pose end) {
        return new DriveToTargetPedroPathCommand(follower, start, end, true);
    }

    public Command startMove(Pose start, Pose end, PathType pathType, double maxPower) {
        return new DriveCommand(follower, start, end, pathType, true).withTempMaxPower(maxPower);
//        return new DriveToTargetPedroPathCommand(follower, start, end, true).withTempMaxPower(maxPower);
    }

    public Command moveTo(Pose end, PathType pathType) {
        return new DriveCommand(follower, end, pathType, false);
//        return new DriveToTargetPedroPathCommand(follower, end, false);
    }

    public Command moveTo(Pose end, PathType pathType, double maxPower) {
        return new DriveCommand(follower, end, pathType, false).withTempMaxPower(maxPower);
    }

    /**
     * turn on chamber roller then intake roller.
     */
    public Command startIntake() {
        return new InstantCommand(transferChamber::TurnOnChamberRoller).andThen(new InstantCommand(intake::StartIntake));
    }

    /**
     * Turn off intake roller then chamber roller.
     */
    public Command stopIntake() {
        return new InstantCommand(intake::StopIntake).andThen(new InstantCommand(transferChamber::TurnOffChamberRoller));
    }

    public Command intake() {
        return sleep(1000);
    }

    /**
     * intake 3 or 2 seconds timeout
     */
    public Command intakeRowDeadline() {
        return new ParallelRaceGroup(
                sleep(2000),
                waitFrontArtifact()
                        .andThen(waitFrontArtifact())
                        .andThen(waitFrontArtifact())
        );
    }

    public Command waitFrontArtifact() {
        return new WaitUntilFrontArtifactIntaken(telemetry, transferChamber)
                .andThen(new InstantCommand(transferChamber::IncrementArtifactCount));
    }

    public Command intakeRow() {
        return new IntakeRowCommand(transferChamber, intake, telemetry, DEFAULT_TIME_OUT);
    }

    public Command topRollerOutput() {
        return new SingleExecuteCommand(transferChamber::TopRollersOuttake);
    }

    public Command stopTopRoller() {
        return new SingleExecuteCommand(transferChamber::TopRollersStop);
    }

//    public Command ballReset() {
//        return new SingleExecuteCommand(transferChamber::BallReset);
//    }

    public Command loadArtifact() {
        return new ParallelDeadlineGroup(
                new WaitUntilTopArtifactSeen(telemetry, transferChamber),
                new SingleExecuteCommand(transferChamber::TurnOnSlowChamberRoller));
    }

    public Command turnOffChamberRoller() {
        return new SingleExecuteCommand(transferChamber::TurnOffChamberRoller);
    }

//    public Command feedArtifact() {
//        return new SingleExecuteCommand(transferChamber::FeedArtifact);
//    }
//
//    public Command resetFeeder() {
//        return new SingleExecuteCommand(transferChamber::ResetFeeder);
//    }

//    public Command ballStow() {
//        return new SingleExecuteCommand(transferChamber::BallStow);
//    }
//
//    public Command ballLaunch() {
//        return new SingleExecuteCommand(transferChamber::BallLaunch);
//    }

    public Command farShoot() {
        return new SingleExecuteCommand(shooter::FarShoot);
    }

    public Command closeShoot() {
        return new SingleExecuteCommand(shooter::CloseShoot);
    }

    public Command farShootWithScale(double scale, double elevationScale) {
        return new SingleExecuteCommand(() -> shooter.FarShootWithScale(scale, elevationScale));
    }

    public Command closeShootWithScale(double scale, double elevationScale) {
        return new SingleExecuteCommand(() -> shooter.CloseShootWithScale(scale, elevationScale));
    }

    public Command loadAndShoot(Command shootCommand) {
        return new ParallelDeadlineGroup(
                sleep(AutonCommonConfigs.shootRowTimeoutInMS),
                loadArtifact()
                        .andThen(shootCommand)
                        .andThen(waitForShooterReady())
                        .andThen(topRollerOutput())

        );
//        long transferDelay = 200;
//        return ballReset()
////                .andThen(resetFeeder())
//                .andThen(shootCommand)
//                .andThen(loadArtifact())
//                .andThen(sleep(transferDelay))
//                .andThen(turnOffChamberRoller())
//                .andThen(sleep(transferDelay))
////                .andThen(feedArtifact())
//                .andThen(sleep(transferDelay))
//                .andThen(ballStow())
////                .andThen(resetFeeder())
//                .andThen(waitForShooterReady())
//                .andThen(sleep(transferDelay + 200))
//                .andThen(ballLaunch());
    }

    public Pose getCurrentFollowerPose() {
        return follower.getPose();
    }

    public Command waitForShooterReady() {
        return new WaitShooterReadyCommand(DEFAULT_TIME_OUT, shooter);
    }

}
