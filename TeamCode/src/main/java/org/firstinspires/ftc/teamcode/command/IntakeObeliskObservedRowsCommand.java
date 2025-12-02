package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.arcrobotics.ftclib.command.Command;
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

    @Override
    protected void firstTimeExecute() {
        if (result == null || !result.isScheduled()) {
            List<RowsOnFloor> rowsOnFloors = CommandRuntimeSharedProperties.computeRowSequence(shootRange, true);

            Log.i(LOG_TAG, "Rows to intake: " + rowsOnFloors.stream().map(RowsOnFloor::name).collect(Collectors.joining(", ")));
            result = commandFactory.noop();
            for (RowsOnFloor row : rowsOnFloors) {
                result = result.andThen(commandFactory.intakeRowAndShoot(
                        getRowStartingPosition(row),
                        getRowEndingPosition(row),
                        getIntakeDriveTrainPower(),
                        getRowShootingPosition(),
                        shootRange,
                        row,
                        true
                ));
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

    public double getIntakeDriveTrainPower() {
        return AutonCommonConfigs.DrivetrainIntakePower * positions.getDriveTrainIntakePowerScale();
    }
}
