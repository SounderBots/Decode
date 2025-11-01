package org.firstinspires.ftc.teamcode.command;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Transfer;

public class WaitUntilTopArtifactIntaken extends SounderBotCommandBase {

    private final Transfer transfer;
    private Telemetry telemetry;

    public WaitUntilTopArtifactIntaken(Telemetry telemetry, Transfer transfer)
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
        telemetry.addLine("waiting for top sensor");
        telemetry.update();
    }

    @Override
    protected boolean isTargetReached() {
        return !transfer.IsArtifactDetected();
    }
}
