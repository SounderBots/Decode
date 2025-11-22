package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.arcrobotics.ftclib.command.Command;
import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.command.PathType;

@Autonomous(name="Intake and shoot test", group="Test")
@Configurable
public class IntakeAndShootTest extends CommandAutoOpMode {

    public static double endX = 40;
    public static double endY = 0;

    @Override
    protected Command createCommand() {
        Command shootCommand = commandFactory.farShootWithScale(AutonCommonConfigs.backShootVelocityScale, AutonCommonConfigs.TiltServoLo);
        return commandFactory.startMove(new Pose(0, 0, 0), new Pose(endX, endY, 0), PathType.LINE, AutonCommonConfigs.DrivetrainIntakePower)
                .alongWith(commandFactory.intakeRow())
                .andThen(commandFactory.loadAndShoot(shootCommand, true));
    }
}
