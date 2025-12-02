package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.Subsystem;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class WrapperCommand extends SounderBotCommandBase {

    final SounderBotCommandBase delegate;

    @Override
    protected void onTimeout() {
        delegate.onTimeout();
    }


    @Override
    public void end(boolean interrupted) {
        delegate.end(interrupted);
    }

    @Override
    protected void firstTimeExecute() {
        delegate.firstTimeExecute();
    }

    @Override
    protected boolean isDebugging() {
        return delegate.isDebugging();
    }

    @Override
    protected void onFlagEnabled(boolean flag, Runnable runnable) {
        delegate.onFlagEnabled(flag, runnable);
    }

    @Override
    public Set<Subsystem> getRequirements() {
        return delegate.getRequirements();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public String getSubsystem() {
        return delegate.getSubsystem();
    }

    @Override
    public void setSubsystem(String subsystem) {
        delegate.setSubsystem(subsystem);
    }

    @Override
    public void initialize() {
        delegate.initialize();
    }

    @Override
    public Command withTimeout(long millis) {
        return delegate.withTimeout(millis);
    }

    @Override
    public Command interruptOn(BooleanSupplier condition) {
        return delegate.interruptOn(condition);
    }

    public WrapperCommand(SounderBotCommandBase delegate) {
        super(delegate.getTimeoutMs());
        this.delegate = delegate;
    }

    @Override
    protected void doExecute() {
        delegate.doExecute();
    }

    @Override
    public boolean hasRequirement(Subsystem requirement) {
        return delegate.hasRequirement(requirement);
    }

    @Override
    public void schedule(boolean interruptible) {
        delegate.schedule(interruptible);
    }

    @Override
    public void schedule() {
        delegate.schedule();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }

    @Override
    public boolean isScheduled() {
        return delegate.isScheduled();
    }

    @Override
    public boolean runsWhenDisabled() {
        return delegate.runsWhenDisabled();
    }

    @Override
    protected boolean isTargetReached() {
        return delegate.isTargetReached();
    }
}
