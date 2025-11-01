package org.firstinspires.ftc.teamcode.command;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.TransferChamber;

public class WaitUntilTopArtifactSeen extends SounderBotCommandBase {

    private final TransferChamber transfer;
    private final Telemetry telemetry;

    public WaitUntilTopArtifactSeen(Telemetry telemetry, TransferChamber transfer)
    {
        super(3 * 1000);

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
        return transfer.IsArtifactDetected();
    }
}
