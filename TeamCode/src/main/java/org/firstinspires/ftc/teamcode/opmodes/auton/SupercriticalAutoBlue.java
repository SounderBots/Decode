package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.BlueShortPositions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;

@Autonomous(name="SupercriticalAutoBlue", group="Blue")
@Configurable
public class SupercriticalAutoBlue extends BlueShootFromFront {

    @Override
    public Positions getPositions() {

        return new BlueShortPositions() {

            @Override
            public boolean observeObelisk() {
                return false;
            }

            @Override
            public boolean openGateBetweenPPGAndPGP() {
                return false;
            }
        };

    }
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
}
