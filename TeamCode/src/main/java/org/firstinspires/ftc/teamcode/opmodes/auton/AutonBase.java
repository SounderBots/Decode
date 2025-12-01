package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.command.PathType;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.AutonCommonConfigs;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.BlueLongPositions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.BlueShortPositions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.RedLongPositions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.RedShortPositions;

import java.util.List;

public abstract class AutonBase extends CommandAutoOpMode {

    @Override
    protected Command createCommand() {
        Command command = moveAndShootPreloads();
        List<RowsOnFloor> rowSequence = getRowSequence();
        for (RowsOnFloor row : rowSequence) {
            command = command.andThen(intakeRowAndShoot(row, true));
        }

        return moveOutAtLastSecond(command);
    }

    protected Command moveAndShootPreloads() {
        return commandFactory
                .startMove(getStartingPosition(), getPreloadShootPosition(), PathType.LINE, .6) // move to shooting position
                .andThen(getShootRowCommand(false)) // shoot preloads
                ;
    }

    protected List<RowsOnFloor> getRowSequence() {
        return switch (shootRange()) {
            case LONG -> List.of(RowsOnFloor.GPP/*, RowsOnFloor.SECOND, RowsOnFloor.THIRD*/);
            case SHORT -> List.of(RowsOnFloor.PPG, RowsOnFloor.PGP/*, RowsOnFloor.FIRST*/);
        };
    }

    protected Command intakeRowAndShoot(RowsOnFloor row, boolean shoot) {
        Pose rowStartingPosition = getRowStartingPosition(row);
        double driveMaxPower = switch (row) {
            case GPP ->
                    switch (shootRange()) {
                        case LONG -> AutonCommonConfigs.slowMoveSpeed;
                        case SHORT -> AutonCommonConfigs.fastMoveSpeed;
                    };
            case PGP -> AutonCommonConfigs.middleMoveSpeed;
            case PPG ->
                    switch (shootRange()) {
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
        return new ParallelRaceGroup(
                commandFactory.moveTo(rowEndPose, PathType.LINE, getIntakeDriveTrainPower()),
                commandFactory.intakeRow()
        );
    }

    protected Command moveOutAtLastSecond(Command autonCommand) {
        return new ParallelDeadlineGroup(commandFactory.sleep(29000),
                autonCommand).andThen(commandFactory.moveTo(getFinishPosition(), PathType.LINE));
    }

    Pose getRowStartingPosition(RowsOnFloor row) {
        Positions positions = getPositions();
        return switch (row) {
            case GPP -> positions.getGPPStartPosition();
            case PGP -> positions.getPGPStartPosition();
            case PPG -> positions.getPPGStartPosition();
            default -> getFinishPosition();
        };
    }

    Pose getRowEndingPosition(RowsOnFloor row) {
        Positions positions = getPositions();
        return switch (row) {
            case GPP -> positions.getGPPEndPosition();
            case PGP -> positions.getPGPEndPosition();
            case PPG -> positions.getPPGEndPosition();
            default -> getFinishPosition();
        };
    }



    Pose getStartingPosition() {
        return switch (shootRange()) {
            case SHORT -> getPositions().getShortStartPosition();
            case LONG -> getPositions().getLongStartPosition();
        };
    }

    abstract Side getSide();


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

    protected Pose getFinishPosition() {
        return switch (shootRange()) {
            case LONG -> getPositions().getLongFinishPosition();
            case SHORT -> getPositions().getShortFinishPosition();
        };
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
        return switch (shootRange()) {
            case LONG -> getPositions().getLongPreloadShootPosition();
            case SHORT -> getPositions().getShortPreloadShootPosition();
        };
//        if (shootRange() == ShootRange.LONG) {
//            return getPositions().getLongPreloadShootPosition();
//        }
//        return getRowShootingPosition();
    }

    public Command observeObelisk() {
        return commandFactory.moveTo(getPositions().getObeliskObservePosition(), PathType.LINE, .6);
    }

}
