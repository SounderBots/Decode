package org.firstinspires.ftc.teamcode.command;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Transfer;

public class WaitUntilFrontArtifactIntaken extends SounderBotCommandBase {

    private final Transfer transfer;
    private final Telemetry telemetry;

    public WaitUntilFrontArtifactIntaken(Telemetry telemetry, Transfer transfer)
    {
        super(3 * 1000); // 30m wait

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
        return !transfer.IsFrontArtifactDetected();
    }
}
