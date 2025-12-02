package org.firstinspires.ftc.teamcode.command;

import static org.firstinspires.ftc.teamcode.command.CommandRuntimeSharedProperties.computeRowSequence;

import android.util.Log;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.SounderBotParallelRaceGroup;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.AutonCommonConfigs;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class IntakeObeliskObservedRowsCommand extends SounderBotCommandBase{

    private static final String LOG_TAG = IntakeObeliskObservedRowsCommand.class.getSimpleName();
    public static final long TIMEOUT_MS = TimeUnit.SECONDS.toMillis(25); //25 seconds

    private final ShootRange shootRange;
    private final Positions positions;
    private final CommandFactory commandFactory;
    Command result;

    public IntakeObeliskObservedRowsCommand(ShootRange shootRange, Positions positions, CommandFactory commandFactory) {
        super(TIMEOUT_MS);
        this.shootRange = shootRange;
        this.positions = positions;
        this.commandFactory = commandFactory;
    }

//    @Override
//    public void end(boolean interrupted) {
//        if (interrupted) {
//            result.cancel();
//        }
//    }

    @Override
    protected void firstTimeExecute() {
        if (result == null || !result.isScheduled()) {
            List<RowsOnFloor> rowsOnFloors = CommandRuntimeSharedProperties.computeRowSequence(shootRange, true);

            Log.i(LOG_TAG, "rowsOnFloors: " + rowsOnFloors.stream().map(RowsOnFloor::name).collect(Collectors.joining(", ")));
            result = commandFactory.noop();
            for (RowsOnFloor row : rowsOnFloors) {
                result = result.andThen(intakeRowAndShoot(row, true));
            }
            result.schedule();
            CommandRuntimeSharedProperties.observedObeliskShowedRow = RowsOnFloor.NOT_TRIED;
        }
    }

    @Override
    protected void doExecute() {
    }

    @Override
    protected boolean isTargetReached() {
        return result.isFinished();
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
        return switch (row) {
            case GPP -> positions.getGPPStartPosition();
            case PGP -> positions.getPGPStartPosition();
            case PPG -> positions.getPPGStartPosition();
            default -> getFinishPosition();
        };
    }

    Pose getRowEndingPosition(RowsOnFloor row) {
        return switch (row) {
            case GPP -> positions.getGPPEndPosition();
            case PGP -> positions.getPGPEndPosition();
            case PPG -> positions.getPPGEndPosition();
            default -> getFinishPosition();
        };
    }

    protected Pose getFinishPosition() {
        return switch (shootRange) {
            case LONG -> positions.getLongFinishPosition();
            case SHORT -> positions.getShortFinishPosition();
        };
    }

    protected Pose getRowShootingPosition() {
        return switch (shootRange) {
            case SHORT -> positions.getShortShootPosition();
            case LONG -> positions.getLongShootPosition();
        };
    }

    protected Command intakeRowAndShoot(RowsOnFloor row, boolean shoot) {
        Pose rowStartingPosition = getRowStartingPosition(row);
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
                ? commandFactory.moveTo(rowStartingPosition, PathType.LINE, driveMaxPower).andThen(commandFactory.moveTo(getRowShootingPosition(), PathType.LINE, driveMaxPower))
                : commandFactory.moveTo(getRowShootingPosition(), PathType.LINE, driveMaxPower);
        return commandFactory
                .moveTo(rowStartingPosition, PathType.CURVE, driveMaxPower)
                .andThen(intakeRow(row)) // intake row (3 balls)
                .andThen(driveToShootCommand) // move to shooting position
                .andThen(shoot ? getShootRowCommand(true) : commandFactory.noop()) // shoot row
                ;
    }

    protected Command intakeRow(RowsOnFloor row) {
        Pose rowEndPose = getRowEndingPosition(row);
//        return commandFactory.moveTo(rowEndPose, PathType.LINE, getIntakeDriveTrainPower()).alongWith(commandFactory.intakeRow());
        return new SounderBotParallelRaceGroup(
                commandFactory.moveTo(rowEndPose, PathType.LINE, getIntakeDriveTrainPower()),
                commandFactory.intakeRow()
        );
    }

    public Command getShootRowCommand(boolean loadFirst) {
        return commandFactory.loadAndShoot(getShootCommand(), loadFirst);
    }

    public Command getShootCommand() {
        return switch (shootRange) {
            case LONG -> commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale, AutonCommonConfigs.TiltServoLo);
            case SHORT -> commandFactory.closeShootWithScale(AutonCommonConfigs.frontShootVelocityScale, AutonCommonConfigs.TiltServoHi);
        };
    }

    public double getIntakeDriveTrainPower() {
        return AutonCommonConfigs.DrivetrainIntakePower * positions.getDriveTrainIntakePowerScale();
    }
}
