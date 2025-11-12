package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.pedropathing.geometry.Pose;

public abstract class AutonBase extends CommandAutoOpMode {

    protected Command intakeRowAndShoot(RowsOnFloor row) {
        Pose rowStartingPosition = getRowStartingPosition(row);

        return commandFactory
                .moveTo(rowStartingPosition, .7)
                .andThen(intakeRow(row)) // intake row (3 balls)
                .andThen(commandFactory.moveTo(getRowShootingPosition(), .7)) // move to shooting position
                .andThen(getShootRowCommand()) // shoot row
        ;
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
        RowOnFloorPositions positions = getPositions();
        return switch (row) {
            case FIRST -> positions.getFirstRowStartPosition();
            case SECOND -> positions.getSecondRowStartPosition();
            case THIRD -> positions.getThirdRowStartPosition();
        };
    }

    Pose getRowEndingPosition(RowsOnFloor row) {
        RowOnFloorPositions positions = getPositions();
        return switch (row) {
            case FIRST -> positions.getFirstRowEndPosition();
            case SECOND -> positions.getSecondRowEndPosition();
            case THIRD -> positions.getThirdRowEndPosition();
        };
    }

    protected Command moveAndShootPreloads() {
        return commandFactory
                .startMove(getStartingPosition(), getRowShootingPosition(), .7) // move to shooting position
                .andThen(getShootRowCommand()) // shoot preloads
        ;
    }

    Pose getStartingPosition() {
        return switch (shootMode()) {
            case CLOSE -> getPositions().getFrontStartPosition();
            case FAR -> getPositions().getBackStartPosition();
        };
    }

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
//                .andThen(alignWithFirstRow())
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
                .andThen(intakeRowAndShoot(RowsOnFloor.SECOND))
                .andThen(intakeRowAndShoot(RowsOnFloor.THIRD))
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
            case FAR -> commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale);
            case CLOSE -> commandFactory.closeShootWithScale(AutonCommonConfigs.frontShootVelocityScale);
        };
    }

    public Command getShootRowCommand() {
        return commandFactory.loadAndShoot(getShootCommand())
                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
                .andThen(commandFactory.loadAndShoot(getShootCommand()))
                .andThen(commandFactory.sleep(AutonCommonConfigs.betweenShootDelays))
                .andThen(commandFactory.loadAndShoot(getShootCommand()));
    }

    protected Pose getRowShootingPosition() {
        return switch (shootMode()) {
            case CLOSE -> getPositions().getFrontShootPosition();
            case FAR -> getPositions().getBackShootPosition();
        };
    }

    protected Command moveOutAtLastSecond(Command autonCommand) {
        return new ParallelDeadlineGroup(commandFactory.sleep(29000),
                autonCommand).andThen(commandFactory.moveTo(getFinishPosition()));
    }

    protected Pose getFinishPosition() {
        return switch (shootMode()) {
            case FAR -> getPositions().getBackFinishPosition();
            case CLOSE -> getPositions().getFrontFinishPosition();
        };
    }

    @Override
    protected Command createCommand() {
        return switch (shootMode()) {
            case FAR -> moveOutAtLastSecond(shootFromBackCommand());
            case CLOSE -> moveOutAtLastSecond(shootFromFrontCommand());
        };
    }

    public RowOnFloorPositions getPositions() {
        return switch (getSide()) {
            case RED -> new RedPositions();
            case BLUE -> new BluePositions();
        };
//        return switch (getSide()) {
//            case RED -> switch (shootMode()) {
//                    case FAR -> new RedSideBackRowsOnFloorPositions();
//                    case CLOSE -> new RedSideFrontRowsOnFloorPositions();
//                };
//            case BLUE -> switch (shootMode()) {
//                case FAR -> new BlueSideBackRowsOnFloorPositions();
//                case CLOSE -> new BlueSideFrontRowsOnFloorPositions();
//            };
//
//        };
    }
}
