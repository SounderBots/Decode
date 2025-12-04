package org.firstinspires.ftc.teamcode.command;

import java.util.ArrayList;
import java.util.List;

public class AutoAlignToShoot extends SounderBotCommandBase {

    public class UntilCallback {
        public double afterStartMs;
        public Runnable callback;


        public UntilCallback(double afterStartMs, Runnable callback) {
            this.afterStartMs = afterStartMs;
            this.callback = callback;
        }
    }

    public List<UntilCallback> callbacks = new ArrayList<>();

    public AutoAlignToShoot(long timeOut) {
        super(timeOut);
    }

    @Override
    protected void doExecute() {

    }

    @Override
    protected boolean isTargetReached() {
        return isTimeout();
    }
}
