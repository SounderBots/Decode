package org.firstinspires.ftc.teamcode.command;

import org.firstinspires.ftc.teamcode.common.AprilTagEnum;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;

import java.util.Optional;

public class ObserveObeliskCommand extends SounderBotCommandBase {

    final LimeLightAlign limeLight;
    private static final long TIMEOUT_MS = 2000;
    boolean observed;

    public ObserveObeliskCommand(LimeLightAlign limeLight) {
        super(TIMEOUT_MS);
        this.limeLight = limeLight;
        this.observed = false;
    }

    @Override
    protected void doExecute() {
        if (limeLight == null) {
            observed = true;
            CommandRuntimeSharedProperties.observedObeliskShowedRow = RowsOnFloor.NONE;
            return;
        }

        Optional<RowsOnFloor> result = limeLight.getObeliskAprilTag().flatMap(AprilTagEnum::toRowsOnFloor);
        if (result.isPresent()) {
            observed = true;
            CommandRuntimeSharedProperties.observedObeliskShowedRow = result.get();
        }
    }

    @Override
    protected boolean isTargetReached() {
        return observed;
    }
}
