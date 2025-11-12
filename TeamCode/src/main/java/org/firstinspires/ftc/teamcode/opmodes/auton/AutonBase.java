package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.pedropathing.geometry.Pose;

import java.util.List;

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
                .startMove(getStartingPosition(), getRowShootingPosition(), .7) // move to shooting position
                .andThen(getShootRowCommand()) // shoot preloads
        ;
    }

    Pose getStartingPosition() {
        return switch (shootRange()) {
            case SHORT -> getPositions().getFrontStartPosition();
            case LONG -> getPositions().getBackStartPosition();
        };
    }

    abstract Side getSide();

    protected List<RowsOnFloor> getRowSequence() {
        return switch (shootRange()) {
            case LONG -> List.of(RowsOnFloor.FIRST, RowsOnFloor.SECOND, RowsOnFloor.THIRD);
            case SHORT -> List.of(RowsOnFloor.THIRD, RowsOnFloor.SECOND, RowsOnFloor.FIRST);
        };
    }

    protected Command intakeRow(RowsOnFloor row) {
        Pose rowEndPose = getRowEndingPosition(row);
        return new ParallelRaceGroup(
                commandFactory.moveTo(rowEndPose, AutonCommonConfigs.DrivetrainIntakePower),
                commandFactory.intakeRow()
        );
    }

    protected abstract ShootRange shootRange();

    public Command getShootCommand() {
        return switch (shootRange()) {
            case LONG -> commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale);
            case SHORT -> commandFactory.closeShootWithScale(AutonCommonConfigs.frontShootVelocityScale);
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
        return switch (shootRange()) {
            case SHORT -> getPositions().getFrontShootPosition();
            case LONG -> getPositions().getBackShootPosition();
        };
    }

    protected Command moveOutAtLastSecond(Command autonCommand) {
        return new ParallelDeadlineGroup(commandFactory.sleep(29000),
                autonCommand).andThen(commandFactory.moveTo(getFinishPosition()));
    }

    protected Pose getFinishPosition() {
        return switch (shootRange()) {
            case LONG -> getPositions().getBackFinishPosition();
            case SHORT -> getPositions().getFrontFinishPosition();
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
            case RED -> new RedPositions();
            case BLUE -> new BluePositions();
        };
    }
}
