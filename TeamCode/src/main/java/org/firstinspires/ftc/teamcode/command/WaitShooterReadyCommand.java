package org.firstinspires.ftc.teamcode.command;

import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;

public class WaitShooterReadyCommand extends SounderBotCommandBase {

    public WaitShooterReadyCommand(long timeOut, Shooter shooter) {
        super(timeOut);
        this.shooter = shooter;
    }

    private final Shooter shooter;

    @Override
    protected void doExecute() {
        // do nothing
    }

    @Override
    protected boolean isTargetReached() {
        return shooter.isReadyToShoot();
    }
}
