package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.pedropathing.geometry.Pose;

public abstract class AutonBase extends CommandAutoOpMode {

    protected Command intakeRowAndShoot(RowsOnFloor row) {
        Pose rowStartingPosition = getRowStartingPosition(row);

        return commandFactory
                .moveTo(rowStartingPosition)
                .andThen(intakeRow(row)) // intake row (3 balls)
                .andThen(commandFactory.moveTo(getShootingPosition())) // move to shooting position
                .andThen(commandFactory.shoot()) // shoot row
        ;
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
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
                .startMove(getStartingPosition(), getShootingPosition()) // move to shooting position
                .andThen(commandFactory.shoot()) // shoot preloads
        ;
    }

    abstract Pose getShootingPosition();

    abstract Pose getStartingPosition();

    abstract Side getSide();

    protected Command shootFromBackCommand() {
        return moveAndShootPreloads() // move to shooting position
                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST)) // shoot first row
                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND)) // shoot second row
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD)) // shoot third row
                ;
    }

    protected Command shootFromFrontCommand() {
        return moveAndShootPreloads()
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND))
                .andThen(intakeRowAndShoot(RowsOnFloor.FIRST))
                ;
    }

    protected Command intakeRow(RowsOnFloor row) {
        Pose rowEndPose = getRowEndingPosition(row);
        return commandFactory.prepareIntake().andThen(commandFactory.moveTo(rowEndPose).alongWith(commandFactory.intake()));
    }
}
