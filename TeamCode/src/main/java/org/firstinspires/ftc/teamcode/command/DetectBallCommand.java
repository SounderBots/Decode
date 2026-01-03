package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import org.firstinspires.ftc.teamcode.common.BallPosition;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;

import java.util.Optional;

public class DetectBallCommand extends SounderBotCommandBase {

    private static final String LOG_TAG = DetectBallCommand.class.getSimpleName();
    final LimeLightAlign limeLight;
    private static final long TIMEOUT_MS = 2000;
    boolean observed;

    public DetectBallCommand(LimeLightAlign limeLight) {
        super(TIMEOUT_MS);
        this.limeLight = limeLight;
        this.observed = false;
    }

    @Override
    protected void doExecute() {
        if (limeLight == null) {
            observed = true;
            return;
        }

        Optional<BallPosition> result = limeLight.getBallPosition();
        if (result.isPresent()) {
            observed = true;
            Log.i(LOG_TAG, "ball position result: " + result.get());
        }
    }

    @Override
    protected boolean isTargetReached() {
        return observed;
    }
}
