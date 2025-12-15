package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.Side;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.BlueShortPositions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.RedShortPositions;

@Autonomous(name="SupercriticalAutoRed", group="Red")
@Configurable
public class SupercriticalAutoRed extends AutonBase {

    @Override
    protected Command createCommand() {
        if (isDriveConsiderStopError()) {
            commandFactory.driveTrainConsiderVError();
        }
        boolean shouldObserveObelisk = (shootRange() == ShootRange.SHORT) && getPositions().observeObelisk();
        Command command =
                commandFactory.shooterAutoAlign()
                .andThen(moveAndShootPreloads())
                .andThen(commandFactory.shootRows(shootRange(), getPositions()))
                .andThen(intakeRowAndShoot(RowsOnFloor.PPG, true));
        return moveOutAtLastSecond(command);
    }
    @Override
    boolean isDriveConsiderStopError() {
        return true;
    }
    @Override
    Side getSide() {
        return Side.RED;
    }

    @Override
    protected ShootRange shootRange() {
        return ShootRange.SHORT;
    }
}
