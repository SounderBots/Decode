package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Red back move only", group="Red")
@Configurable
public class RedFromBackMoveTest extends RedShootFromBack {

    @Override
    protected Command createCommand() {
        return commandFactory.startMove(startPosition, preloadShootingPosition)
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getRowStartingPosition(RowsOnFloor.FIRST)))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getRowEndingPosition(RowsOnFloor.FIRST), .3))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(rowShootingPosition))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getRowStartingPosition(RowsOnFloor.SECOND)))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getRowEndingPosition(RowsOnFloor.SECOND), .3))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(rowShootingPosition))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getRowStartingPosition(RowsOnFloor.THIRD)))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getRowEndingPosition(RowsOnFloor.THIRD), .3))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(rowShootingPosition))
                .andThen(commandFactory.sleep(1000))
                .andThen(commandFactory.moveTo(getFinishPosition()))
                ;

    }
}
