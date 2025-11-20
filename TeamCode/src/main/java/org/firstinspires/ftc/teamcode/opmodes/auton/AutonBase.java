package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.command.PathType;

import java.util.List;

public abstract class AutonBase extends CommandAutoOpMode {

    protected Command intakeRowAndShoot(RowsOnFloor row) {
        Pose rowStartingPosition = getRowStartingPosition(row);
        double driveMaxPower = switch (row) {
            case FIRST ->
                switch (shootRange()) {
                    case LONG -> AutonCommonConfigs.slowMoveSpeed;
                    case SHORT -> AutonCommonConfigs.fastMoveSpeed;
                };
            case SECOND -> AutonCommonConfigs.middleMoveSpeed;
            case THIRD ->
                switch (shootRange()) {
                    case LONG -> AutonCommonConfigs.fastMoveSpeed;
                    case SHORT -> AutonCommonConfigs.slowMoveSpeed;
                };
        };

        boolean isSecondRow = row == RowsOnFloor.SECOND;

        Command driveToShootCommand = isSecondRow
                ? commandFactory.moveTo(rowStartingPosition, PathType.LINE, driveMaxPower).andThen(commandFactory.moveTo(getRowShootingPosition(), PathType.LINE, driveMaxPower))
                : commandFactory.moveTo(getRowShootingPosition(), PathType.LINE, driveMaxPower);
        return commandFactory
                .moveTo(rowStartingPosition, PathType.CURVE, driveMaxPower)
                .andThen(intakeRow(row)) // intake row (3 balls)
                .andThen(driveToShootCommand) // move to shooting position
                .andThen(getShootRowCommand(true)) // shoot row
        ;
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
        Positions positions = getPositions();
        return switch (row) {
            case FIRST -> positions.getFirstRowStartPosition();
            case SECOND -> positions.getSecondRowStartPosition();
            case THIRD -> positions.getThirdRowStartPosition();
        };
    }

    Pose getRowEndingPosition(RowsOnFloor row) {
        Positions positions = getPositions();
        return switch (row) {
            case FIRST -> positions.getFirstRowEndPosition();
            case SECOND -> positions.getSecondRowEndPosition();
            case THIRD -> positions.getThirdRowEndPosition();
        };
    }

    protected Command moveAndShootPreloads() {
        return commandFactory
                .startMove(getStartingPosition(), getPreloadShootPosition(), PathType.LINE, .6) // move to shooting position
                .andThen(getShootRowCommand(false)) // shoot preloads
        ;
    }

    Pose getStartingPosition() {
        return switch (shootRange()) {
            case SHORT -> getPositions().getShortStartPosition();
            case LONG -> getPositions().getLongStartPosition();
        };
    }

    abstract Side getSide();

    protected List<RowsOnFloor> getRowSequence() {
        return switch (shootRange()) {
            case LONG -> List.of(RowsOnFloor.FIRST, RowsOnFloor.SECOND/*, RowsOnFloor.THIRD*/);
            case SHORT -> List.of(RowsOnFloor.THIRD, RowsOnFloor.SECOND/*, RowsOnFloor.FIRST*/);
        };
    }

    protected Command intakeRow(RowsOnFloor row) {
        Pose rowEndPose = getRowEndingPosition(row);
        return new ParallelRaceGroup(
                commandFactory.moveTo(rowEndPose, PathType.LINE, getIntakeDriveTrainPower()),
                commandFactory.intakeRow()
        );
    }

    protected abstract ShootRange shootRange();

    public Command getShootCommand() {
        return switch (shootRange()) {
            case LONG -> commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale, AutonCommonConfigs.TiltServoLo);
            case SHORT -> commandFactory.closeShootWithScale(AutonCommonConfigs.frontShootVelocityScale, AutonCommonConfigs.TiltServoHi);
        };
    }

    public Command getShootRowCommand(boolean loadFirst) {
        return commandFactory.loadAndShoot(getShootCommand(), loadFirst);
//                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
//                .andThen(commandFactory.loadAndShoot(getShootCommand()))
//                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
//                .andThen(commandFactory.loadAndShoot(getShootCommand()));
    }

    protected Pose getRowShootingPosition() {
        return switch (shootRange()) {
            case SHORT -> getPositions().getShortShootPosition();
            case LONG -> getPositions().getLongShootPosition();
        };
    }

    protected Command moveOutAtLastSecond(Command autonCommand) {
        return new ParallelDeadlineGroup(commandFactory.sleep(29000),
                autonCommand).andThen(commandFactory.moveTo(getFinishPosition(), PathType.LINE));
    }

    protected Pose getFinishPosition() {
        return switch (shootRange()) {
            case LONG -> getPositions().getLongFinishPosition();
            case SHORT -> getPositions().getShortFinishPosition();
        };
    }

    @Override
    protected Command createCommand() {
        Command command = moveAndShootPreloads();
        List<RowsOnFloor> rowSequence = getRowSequence();
        for (RowsOnFloor row : rowSequence) {
            command = command.andThen(intakeRowAndShoot(row));
        }

        return moveOutAtLastSecond(command);
    }

    public Positions getPositions() {
        return switch (getSide()) {
            case RED -> switch (shootRange()) {
                case SHORT -> new RedShortPositions();
                case LONG -> new RedLongPositions();
            };
            case BLUE -> switch (shootRange()) {
                case SHORT -> new BlueShortPositions();
                case LONG -> new BlueLongPositions();
            };
        };
    }

    public double getIntakeDriveTrainPower() {
        return AutonCommonConfigs.DrivetrainIntakePower * getPositions().getDriveTrainIntakePowerScale();
    }

    public Pose getPreloadShootPosition() {
        if (shootRange() == ShootRange.LONG) {
            return getPositions().getLongPreloadShootPosition();
        }
        return getRowShootingPosition();
    }
}
