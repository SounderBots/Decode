package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SounderBotParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.AutonCommonConfigs;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.AutonDriveTrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.TeleopDrivetrain;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Stopper;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;

import java.util.Arrays;
import java.util.List;
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

    @Getter
    final Stopper stopper;

    final LimeLightAlign limeLightAlign;


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

    public Command startMove(Pose start, Pose end, PathType pathType, double maxPower) {
        return new DriveCommand(follower, start, end, pathType, true).withTempMaxPower(maxPower).withLimeLight(limeLightAlign);
    }

    public Command moveTo(Pose end, PathType pathType) {
        return new DriveCommand(follower, end, pathType, false).withLimeLight(limeLightAlign);
    }

    public Command moveTo(Pose end, PathType pathType, double maxPower) {
        return new DriveCommand(follower, end, pathType, false).withTempMaxPower(maxPower).withLimeLight(limeLightAlign);
    }

    public Command moveTo(Pose end, PathType pathType, double maxPower, long timeoutMs) {
        return new DriveCommand(follower, List.of(follower.getPose(), end), pathType, timeoutMs, TimeUnit.MILLISECONDS, false).withTempMaxPower(maxPower).withLimeLight(limeLightAlign);
    }

    public Command moveToCurve(double maxPower, Pose... poses) {
        return new DriveCommand(follower, Arrays.asList(poses), PathType.CURVE, DriveCommand.DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, false).withLimeLight(limeLightAlign);
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
        return stopperStop().andThen(new IntakeRowCommand(transferChamber, intake, telemetry, DEFAULT_TIME_OUT));
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

    public Command turnOnChamberRoller() {
        return new SingleExecuteCommand(transferChamber::TurnOnChamberRoller);
    }

    public Command turnOnSlowChamberRoller() {
        return new SingleExecuteCommand(transferChamber::TurnOnSlowChamberRoller);
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

    public Command noop() {
        return new InstantCommand(() -> {});
    }

    public Command loadAndShoot(Command shootCommand, boolean loadFirst) {
        return new ParallelDeadlineGroup(
                sleep(loadFirst ? AutonCommonConfigs.shootWithLoadTimeoutInMS : AutonCommonConfigs.shootWithoutLoadTimeoutInMS),
                stopperGo()
                        .andThen(startIntake())
                        .andThen(turnOnChamberRoller())
                        .andThen(shootCommand)
                        .andThen(waitForShooterReady())
                        .andThen(topRollerOutput())

        ).andThen(stopIntake()).andThen(stopTopRoller()).andThen(turnOffChamberRoller());
    }

    public Pose getCurrentFollowerPose() {
        return follower.getPose();
    }

    public Command waitForShooterReady() {
        return new WaitShooterReadyCommand(DEFAULT_TIME_OUT, shooter);
    }

    public Command stopperGo() {
        return new InstantCommand(stopper::Go);
    }

    public Command stopperStop() {
        return new InstantCommand(stopper::Stop);
    }

    public Command observeObelisk() {
        return new ObserveObeliskCommand(limeLightAlign);
    }

    public Command shootRows(ShootRange shootRange, Positions positions) {
        return new IntakeObeliskObservedRowsCommand(shootRange, positions, this);
    }

    protected Command intakeRow(Pose rowEndPose, double driveTrainPower) {
        return new SounderBotParallelRaceGroup(
                moveTo(rowEndPose, PathType.LINE, driveTrainPower),
                intakeRow()
        );
    }

    public Command getShootCommand(ShootRange shootRange) {
        return switch (shootRange) {
            case LONG -> farShootWithScale(AutonCommonConfigs.backShootVelocityScale, AutonCommonConfigs.TiltServoLo);
            case SHORT -> closeShootWithScale(AutonCommonConfigs.frontShootVelocityScale, AutonCommonConfigs.TiltServoHi);
        };
    }

    protected Command intakeRowAndShoot(Pose rowStartingPosition, Pose rowEndingPosition, double intakeDriveTrainPower, Pose rowShootingPosition, ShootRange shootRange, RowsOnFloor row, boolean shoot) {
        double driveMaxPower = switch (row) {
            case GPP ->
                    switch (shootRange) {
                        case LONG -> AutonCommonConfigs.slowMoveSpeed;
                        case SHORT -> AutonCommonConfigs.fastMoveSpeed;
                    };
            case PGP -> AutonCommonConfigs.middleMoveSpeed;
            case PPG ->
                    switch (shootRange) {
                        case LONG -> AutonCommonConfigs.fastMoveSpeed;
                        case SHORT -> AutonCommonConfigs.slowMoveSpeed;
                    };
            default -> AutonCommonConfigs.middleMoveSpeed;
        };

        boolean isSecondRow = row == RowsOnFloor.PGP;

        Command driveToShootCommand = isSecondRow
                ? moveTo(rowStartingPosition, PathType.LINE, driveMaxPower).andThen(moveTo(rowShootingPosition, PathType.LINE, driveMaxPower))
                : moveTo(rowShootingPosition, PathType.LINE, driveMaxPower);
        return moveTo(rowStartingPosition, PathType.CURVE, driveMaxPower)
                .andThen(intakeRow(rowEndingPosition, intakeDriveTrainPower)) // intake row (3 balls)
                .andThen(driveToShootCommand) // move to shooting position
                .andThen(shoot ? loadAndShoot(getShootCommand(shootRange), true) : noop()) // shoot row
                ;
    }

}
