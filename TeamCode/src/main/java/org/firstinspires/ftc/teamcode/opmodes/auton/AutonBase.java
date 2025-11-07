package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.pedropathing.geometry.Pose;

public abstract class AutonBase extends CommandAutoOpMode {

    protected Command intakeRowAndShoot(RowsOnFloor row) {
        Pose rowStartingPosition = getRowStartingPosition(row);

        return commandFactory
                .moveTo(rowStartingPosition)
                .andThen(intakeRow(row)) // intake row (3 balls)
                .andThen(commandFactory.moveTo(getShootingPosition())) // move to shooting position
                .andThen(getShootRowCommand()) // shoot row
        ;
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
//        Pose firstRowPose = switch (getSide()) {
//            case RED -> RedSideRowsOnFloorPositions.firstRowStartingPosition;
//            case BLUE -> BlueSideRowsOnFloorPositions.firstRowStartingPosition;
//        };
//
//        return switch (row) {
//            case FIRST -> firstRowPose;
//            case SECOND -> firstRowPose.copy().withY(firstRowPose.getY() + AutonCommonConfigs.rowDistance);
//            case THIRD -> firstRowPose.copy().withY(firstRowPose.getY() + AutonCommonConfigs.rowDistance * 2);
//        };
        return switch (getSide()) {
            case RED -> switch (row) {
                case FIRST -> RedSideRowsOnFloorPositions.firstRowStartingPosition;
                case SECOND -> RedSideRowsOnFloorPositions.secondRowStartingPosition;
                case THIRD -> RedSideRowsOnFloorPositions.thirdRowStartingPosition;
            };
            case BLUE -> switch (row) {
                case FIRST -> BlueSideRowsOnFloorPositions.firstRowStartingPosition;
                case SECOND -> BlueSideRowsOnFloorPositions.secondRowStartingPosition;
                case THIRD -> BlueSideRowsOnFloorPositions.thirdRowStartingPosition;
            };
        };

    }

    Pose getRowEndingPosition(RowsOnFloor row) {
//        Pose startPos = getRowStartingPosition(row);
//        double xDelta = switch (getSide()) {
//            case RED -> AutonCommonConfigs.intakeDriveDistance * -1;
//            case BLUE -> AutonCommonConfigs.intakeDriveDistance;
//        };
//        return startPos.copy().withX(startPos.getX() + xDelta);

        return switch (getSide()) {
            case RED -> switch (row) {
                case FIRST -> RedSideRowsOnFloorPositions.firstRowEndingPosition;
                case SECOND -> RedSideRowsOnFloorPositions.secondRowEndingPosition;
                case THIRD -> RedSideRowsOnFloorPositions.thirdRowEndingPosition;
            };
            case BLUE -> switch (row) {
                case FIRST -> BlueSideRowsOnFloorPositions.firstRowEndingPosition;
                case SECOND -> BlueSideRowsOnFloorPositions.secondRowEndingPosition;
                case THIRD -> BlueSideRowsOnFloorPositions.thirdRowEndingPosition;
            };
        };
    }

    protected Command moveAndShootPreloads() {
        return commandFactory
                .startMove(getStartingPosition(), getShootingPosition(), .4) // move to shooting position
                .andThen(getShootRowCommand()) // shoot preloads
        ;
    }

    abstract Pose getShootingPosition();

    abstract Pose getStartingPosition();

    abstract Side getSide();

    protected Command shootFromBackCommand() {
        return moveAndShootPreloads() // move to shooting position
                .andThen(alignWithFirstRow())
                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST)) // shoot first row
                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND)) // shoot second row
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD)) // shoot third row
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
                .andThen(alignWithFirstRow())
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND))
                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST))
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
            case FAR -> commandFactory.farShoot();
            case CLOSE -> commandFactory.closeShoot();
        };
    }

    public Command getShootRowCommand() {
        return commandFactory.loadAndShoot(getShootCommand())
                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
                .andThen(commandFactory.loadAndShoot(getShootCommand()))
                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
                .andThen(commandFactory.loadAndShoot(getShootCommand()));
    }
}
