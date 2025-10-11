package org.firstinspires.ftc.teamcode.command;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.AutonDriveTrain;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.TeleopDrivetrain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandFactory {

    final Telemetry telemetry;
    final AutonDriveTrain autonDriveTrain;
    final TeleopDrivetrain teleopDrivetrain;
    final Intake intake;
    final Shooter shooter;

    public Command driveToTarget(DriveToTargetCommand.DriveParameters driveParameters) {
        return new DriveToTargetCommand(autonDriveTrain, telemetry, driveParameters);
    }

    public Command driveToTargetFieldCentric(double targetXInches, double targetYInches, double targetHeadingInDegrees) {
        return new MecanumMoveToTargetCommand(autonDriveTrain.getMecanumDrive(), autonDriveTrain.getOdoX(), autonDriveTrain.getOdoY(), autonDriveTrain.getImu(),
                new Pose2d(targetXInches, targetYInches, new Rotation2d(Math.toRadians(targetHeadingInDegrees))), 2000);
    }

    public Command intake() {
        throw new NotImplementedException();
    }

    public void shoot() {
        throw new NotImplementedException();
    }
}
