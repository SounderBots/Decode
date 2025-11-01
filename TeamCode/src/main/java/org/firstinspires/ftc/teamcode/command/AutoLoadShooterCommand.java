package org.firstinspires.ftc.teamcode.command;


import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Transfer;

public class AutoLoadShooterCommand extends CommandBase {

    private final Transfer transfer;
    private Telemetry telemetry;

    public AutoLoadShooterCommand(Telemetry telemetry, Transfer transfer) {
        this.transfer = transfer;
        this.telemetry = telemetry;
    }

    @Override
    public void initialize() {
        super.initialize();

        transfer.ResetFeeder();
        transfer.BallReset();
    }

    int artifactCount = 0;
    boolean running = false;

    @Override
    public void execute() {
        super.execute();

        if(transfer.GetArtifactCount() == 3 && !transfer.IsAlreadyTryingToLoadArtifactIntoShooter()) {
            running = true;
            SequentialCommandGroup command = new SequentialCommandGroup(
                    new InstantCommand(transfer::TryToLoadArtifactIntoShooter, transfer),
                    new ParallelDeadlineGroup(
                            new WaitUntilTopArtifactSeen(telemetry, transfer),
                            new InstantCommand(transfer::TurnOnSlowChamberRoller, transfer)
                    ),
                    new WaitCommand(200),
                    new InstantCommand(transfer::TurnOffChamberRoller, transfer),
                    new WaitCommand(MainTeleop.MainTeleopConfig.TransferDelay),
                    new InstantCommand(transfer::FeedArtifact, transfer),
                    new WaitCommand(MainTeleop.MainTeleopConfig.TransferDelay),
                    new InstantCommand(transfer::BallStow, transfer),
                    new InstantCommand(transfer::ResetFeeder, transfer),
                    new InstantCommand(transfer::DecrementArtifactCount, transfer),
                    new InstantCommand(transfer::NotTryingToLoadArtifactIntoShooter, transfer)

                    );

            command.schedule();

            artifactCount++;
        }
    }
}
