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

public class WaitUntilFrontArtifactSeen extends SounderBotCommandBase {

    private final Transfer transfer;
    private Telemetry telemetry;

    public WaitUntilFrontArtifactSeen(Telemetry telemetry, Transfer transfer)
    {
        super(30 * 60 * 1000); // 30m wait

        this.transfer = transfer;
        this.telemetry = telemetry;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected void doExecute() {

    }

    @Override
    protected boolean isTargetReached() {
        return transfer.IsFrontArtifactDetected();
    }
}
