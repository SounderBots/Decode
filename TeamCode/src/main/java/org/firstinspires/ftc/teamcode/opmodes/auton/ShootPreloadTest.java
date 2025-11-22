package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

@Autonomous(name="Shoot Preload test", group="Test")
@Configurable
@Disabled
public class ShootPreloadTest extends CommandAutoOpMode {
    @Override
    protected Command createCommand() {
        return commandFactory.loadAndShoot(commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale, AutonCommonConfigs.TiltServoLo), false)
                .andThen(commandFactory.startMove(new Pose(0, 0, 0), new Pose(20, 0, 0)));
    }
}
