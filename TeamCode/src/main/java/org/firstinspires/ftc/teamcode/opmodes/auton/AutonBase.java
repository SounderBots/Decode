package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.pedropathing.geometry.Pose;

public abstract class AutonBase extends CommandAutoOpMode {

    protected Command intakeRowAndShoot(RowsOnFloor row) {
        Pose rowStartingPosition = getRowStartingPosition(row);

        return commandFactory
                .moveTo(rowStartingPosition, .4)
                .andThen(intakeRow(row)) // intake row (3 balls)
                .andThen(commandFactory.moveTo(getRowShootingPosition(), .8)) // move to shooting position
                .andThen(getShootRowCommand()) // shoot row
        ;
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
        RowOnFloorPositions positions = getRowOnFloorPositions();
        return switch (row) {
            case FIRST -> positions.getFirstRowStartPosition();
            case SECOND -> positions.getSecondRowStartPosition();
            case THIRD -> positions.getThirdRowStartPosition();
        };
    }

    Pose getRowEndingPosition(RowsOnFloor row) {
        RowOnFloorPositions positions = getRowOnFloorPositions();
        return switch (row) {
            case FIRST -> positions.getFirstRowEndPosition();
            case SECOND -> positions.getSecondRowEndPosition();
            case THIRD -> positions.getThirdRowEndPosition();
        };
    }

    protected Command moveAndShootPreloads() {
        return commandFactory
                .startMove(getStartingPosition(), getPreloadShootingPosition(), .4) // move to shooting position
                .andThen(getShootRowCommand()) // shoot preloads
        ;
    }

    abstract Pose getPreloadShootingPosition();

    abstract Pose getStartingPosition();

    abstract Side getSide();

    protected Command shootFromBackCommand() {
        return moveAndShootPreloads() // move to shooting position
//                .andThen(alignWithFirstRow())
                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST)) // shoot first row
//                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND)) // shoot second row
//                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD)) // shoot third row
//                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST))
                ;
    }

    protected Command alignWithFirstRow() {
        RowsOnFloor firstRow = switch (shootMode()) {
            case FAR -> RowsOnFloor.FIRST;
            case CLOSE -> RowsOnFloor.THIRD;
        };

        return alignWithRow(firstRow);
    }

    protected Command alignWithMiddleRow() {
        return alignWithRow(RowsOnFloor.SECOND);
    }

    protected Command alignWithLastRow() {
        RowsOnFloor lastRow = switch (shootMode()) {
            case FAR -> RowsOnFloor.THIRD;
            case CLOSE -> RowsOnFloor.FIRST;
        };
        return alignWithRow(lastRow);
    }

    private Command alignWithRow(RowsOnFloor row) {
        Pose currPos = commandFactory.getCurrentFollowerPose();
        Pose firstRowPose = getRowStartingPosition(row);
        Pose alignedPos = firstRowPose.copy().withX(currPos.getX());
        return commandFactory.moveTo(alignedPos, AutonCommonConfigs.DrivetrainIntakePower);
    }

    protected Command shootFromFrontCommand() {
        return moveAndShootPreloads()
//                .andThen(alignWithFirstRow())
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
//                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND))
//                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
                ;
    }

    protected Command intakeRow(RowsOnFloor row) {
        Pose rowEndPose = getRowEndingPosition(row);
        return new ParallelRaceGroup(
                commandFactory.moveTo(rowEndPose, AutonCommonConfigs.DrivetrainIntakePower),
                commandFactory.intakeRow()
        );
    }

    protected abstract ShootMode shootMode();

    public Command getShootCommand() {
        return switch (shootMode()) {
            case FAR -> commandFactory.farShootWithScale(getShootVelocityScale());
            case CLOSE -> commandFactory.closeShootWithScale(getShootVelocityScale());
        };
    }

    public Command getShootRowCommand() {
        return commandFactory.loadAndShoot(getShootCommand())
                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
                .andThen(commandFactory.loadAndShoot(getShootCommand()))
                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
                .andThen(commandFactory.loadAndShoot(getShootCommand()));
    }

    protected double getShootVelocityScale() {
        return 1;
    }

    protected abstract Pose getRowShootingPosition();

    protected Command moveOutAtLastSecond(Command autonCommand) {
        return autonCommand.andThen(commandFactory.moveTo(getFinishPosition()));
//        return new ParallelDeadlineGroup(commandFactory.sleep(29000),
//                autonCommand).andThen(commandFactory.moveTo(getFinishPosition(getSide())));
    }

    protected Pose getFinishPosition() {
        return switch (getSide()) {
            case BLUE -> switch (shootMode()) {
                case CLOSE -> AutonCommonConfigs.blueFrontFinishPosition;
                case FAR -> AutonCommonConfigs.blueBackFinishPosition;
            };
            case RED -> switch (shootMode()) {
                case CLOSE -> AutonCommonConfigs.redFrontFinishPosition;
                case FAR -> AutonCommonConfigs.redBackFinishPosition;
            };
        };
    }

    @Override
    protected Command createCommand() {
        return switch (shootMode()) {
            case FAR -> moveOutAtLastSecond(shootFromBackCommand());
            case CLOSE -> moveOutAtLastSecond(shootFromFrontCommand());
        };
    }

    public RowOnFloorPositions getRowOnFloorPositions() {
        return switch (getSide()) {
            case RED -> switch (shootMode()) {
                    case FAR -> new RedSideBackRowsOnFloorPositions();
                    case CLOSE -> new RedSideFrontRowsOnFloorPositions();
                };
            case BLUE -> switch (shootMode()) {
                case FAR -> new BlueSideBackRowsOnFloorPositions();
                case CLOSE -> new BlueSideFrontRowsOnFloorPositions();
            };

        };
    }
}
