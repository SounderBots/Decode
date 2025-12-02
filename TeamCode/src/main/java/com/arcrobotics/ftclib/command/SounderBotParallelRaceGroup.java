package com.arcrobotics.ftclib.command;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SounderBotParallelRaceGroup extends CommandGroupBase {
    private final Set<Command> m_commands = new HashSet<>();
    private boolean m_runWhenDisabled = true;
    private boolean m_finished = true;

    /**
     * Creates a new ParallelCommandRace. The given commands will be executed simultaneously, and
     * will "race to the finish" - the first command to finish ends the entire command, with all other
     * commands being interrupted.
     *
     * @param commands the commands to include in this group.
     */
    public SounderBotParallelRaceGroup(Command... commands) {
        addCommands(commands);
    }

    @Override
    public final void addCommands(Command... commands) {
        requireUngrouped(commands);

        if (!m_finished) {
            throw new IllegalStateException(
                    "Commands cannot be added to a CommandGroup while the group is running");
        }

        registerGroupedCommands(commands);

        for (Command command : commands) {
            if (!Collections.disjoint(command.getRequirements(), m_requirements)) {
                throw new IllegalArgumentException("Multiple commands in a parallel group cannot"
                        + " require the same subsystems");
            }
            m_commands.add(command);
            m_requirements.addAll(command.getRequirements());
            m_runWhenDisabled &= command.runsWhenDisabled();
        }
    }

    @Override
    public void initialize() {
        m_finished = false;
        for (Command command : m_commands) {
            command.initialize();
        }
    }

    @Override
    public void execute() {
        Command winningCommand = null;

        // 1. Execute all commands and identify the winner
        for (Command command : m_commands) {
            command.execute();
            if (command.isFinished()) {
                winningCommand = command; // Save the winner
            }
        }

        // 2. If a winner was found, handle termination and interruption
        if (winningCommand != null) {
            m_finished = true; // Mark the group as finished

            // Clean up the winner: call end(false) because it finished naturally
            winningCommand.end(false);

            // 3. Immediately interrupt all the losing commands (the "race" part)
            for (Command command : m_commands) {
                // Interrupt if it's not the winner and hasn't already finished itself
                if (command != winningCommand) {
                    if (!command.isFinished()) {
                        command.end(true); // Call end(true) for interruption
                    }
                }
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted) {
            // If we were interrupted (e.g., by the 30-second timer),
            // we must interrupt all running children.
            for (Command command : m_commands) {
                if (!command.isFinished()) {
                    command.end(true);
                }
            }
        }
    }

    @Override
    public boolean isFinished() {
        return m_finished;
    }

    @Override
    public boolean runsWhenDisabled() {
        return m_runWhenDisabled;
    }
}
