package org.firstinspires.ftc.teamcode.command;


import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelDeadlineGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Transfer;

public class AutoIntakeCommand extends CommandBase {

    private final Transfer transfer;
    private final Telemetry telemetry;

    public AutoIntakeCommand(Telemetry telemetry, Transfer transfer) {
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

    @Override
    public void execute() {
        super.execute();

        if (transfer.IsFrontArtifactDetected() && transfer.GetArtifactCount() < 3) {

            SequentialCommandGroup command =  new SequentialCommandGroup(
                    new ParallelDeadlineGroup(
                            new WaitUntilFrontArtifactIntaken(telemetry, transfer),
                            new InstantCommand(transfer::TurnOnChamberRoller, transfer)
                    ),
                    new InstantCommand(transfer::TurnOffChamberRoller, transfer),
                    new InstantCommand(transfer::IncrementArtifactCount, transfer)
            );

            command.schedule();

            artifactCount++;
        }
    }
}
