package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.Command;

import java.util.ArrayList;
import java.util.List;

public class TimeTickerCommand extends SounderBotCommandBase {

    public class UntilCallback {
        public double afterStartMs;
        public Runnable callback;


        public UntilCallback(double afterStartMs, Runnable callback) {
            this.afterStartMs = afterStartMs;
            this.callback = callback;
        }
    }

    public List<UntilCallback> callbacks = new ArrayList<>();

    public TimeTickerCommand(long timeOut) {
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
