package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.CommandBase;

public class SingleExecuteCommand extends CommandBase {
    private static final String TAG = "SingleExecuteCommand";

    private final Runnable runnable;
    private boolean executed = false;

    public SingleExecuteCommand(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void initialize() {
        super.initialize();
        executed = false;
    }

    @Override
    public void execute() {
        super.execute();
        if (!executed) {
            executed = true;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    @Override
    public boolean isFinished() {
        return executed;
    }
}
