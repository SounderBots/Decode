package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

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
        Log.i(TAG, "executed: " + executed);
        if (!executed) {
            Log.i(TAG, "start execute runnable");
            executed = true;
            if (runnable != null) {
                Log.i(TAG, "before execute runnable");
                runnable.run();
                Log.i(TAG, "after execute runnable");
            }
        }
    }

    @Override
    public boolean isFinished() {
        Log.i(TAG, "Finished: " + executed);
        return executed;
    }
}
