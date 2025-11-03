package org.firstinspires.ftc.teamcode.command;


import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;

public class LaunchAndReload extends SounderBotCommandBase {

    private final TransferChamber transfer;
    private final Telemetry telemetry;

    public LaunchAndReload(Telemetry telemetry, TransferChamber transfer) {
        super(30 * 60 * 1000);

        this.transfer = transfer;
        this.telemetry = telemetry;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void doExecute() {
        super.execute();

        if(!transfer.IsAlreadyTryingToLoadArtifactIntoShooter()) {
            SequentialCommandGroup command = new SequentialCommandGroup(
                    new InstantCommand(transfer::TryToLoadArtifactIntoShooter, transfer),
                    new InstantCommand(transfer::BallLaunch, transfer),
                    new WaitCommand(100),
                    new InstantCommand(transfer::BallReset, transfer),
                    new WaitCommand(200),
                    new ParallelDeadlineGroup(
                            new WaitUntilTopArtifactSeen(telemetry, transfer),
                            new InstantCommand(transfer::TurnOnChamberRoller, transfer)
                    ),
                    new WaitCommand(200),
                    new InstantCommand(transfer::TurnOffChamberRoller, transfer),
                    new InstantCommand(transfer::FeedArtifact, transfer),
                    new WaitCommand(MainTeleop.MainTeleopConfig.TransferDelay),
                    new InstantCommand(transfer::BallStow, transfer),
                    new InstantCommand(transfer::ResetFeeder, transfer),
                    new InstantCommand(transfer::DecrementArtifactCount, transfer),
                    new InstantCommand(transfer::NotTryingToLoadArtifactIntoShooter, transfer)
            );

            command.schedule();
        }
    }

    @Override
    protected boolean isTargetReached() {
        return false;
    }
}
