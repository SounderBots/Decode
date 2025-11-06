package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.opmodes.teleop.MainTeleop;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;

public class IntakeRowCommand extends SounderBotCommandBase {

    int artifactCount = 0;
    private final TransferChamber transferChamber;
    private final Intake intake;
    private double chamberRollerOnTime = -1;
    private final Telemetry telemetry;

    public IntakeRowCommand(TransferChamber transferChamber, Intake intake, Telemetry telemetry, long timeOut) {
        super(timeOut);
        this.transferChamber = transferChamber;
        this.intake = intake;
        this.telemetry = telemetry;
    }

    @Override
    protected void doExecute() {
        if (!transferChamber.isChamberRollerOn() && transferChamber.IsFrontArtifactDetected()) {
            transferChamber.TurnOnChamberRoller();
            chamberRollerOnTime = System.currentTimeMillis();
        }

        if (chamberRollerOnTime > 0 && System.currentTimeMillis() - chamberRollerOnTime > MainTeleop.MainTeleopConfig.TransferDelay) {
            transferChamber.TurnOffChamberRoller();
            chamberRollerOnTime = -1;
            artifactCount ++;

        }
        telemetry.addData("Artifacts", artifactCount);
        telemetry.update();
        Log.d("IntakeRowCommand", "Artifacts: " + artifactCount);

    }

    @Override
    protected boolean isTargetReached() {
        return false;//artifactCount >= 3;
    }

    @Override
    protected void firstTimeExecute() {
//        transferChamber.TurnOnChamberRoller();
        transferChamber.ResetFeeder();
        transferChamber.BallReset();
        intake.StartIntake();
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        transferChamber.TurnOffChamberRoller();
        intake.StopIntake();
    }
}
