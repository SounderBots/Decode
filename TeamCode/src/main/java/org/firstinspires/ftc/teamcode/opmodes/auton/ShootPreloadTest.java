package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name="Intake test", group="Test")
@Configurable
public class ShootPreloadTest extends CommandAutoOpMode {
    @Override
    protected Command createCommand() {
        return commandFactory.loadAndShoot(commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale, AutonCommonConfigs.TiltServoLo));
    }
}
