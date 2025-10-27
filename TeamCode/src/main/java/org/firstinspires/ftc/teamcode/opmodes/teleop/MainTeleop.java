package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.command.CommandFactory;
import org.firstinspires.ftc.teamcode.opmodes.OpModeTemplate;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.TeleopDrivetrain;

@TeleOp
public class MainTeleop extends OpModeTemplate {

    TeleopDrivetrain drive;
    Intake intake;

    Shooter shooter;

    CommandFactory commandFactory;

    @Override
    public void initialize() {
        super.initialize();

        this.drive = new TeleopDrivetrain(hardwareMap, driverGamepad, telemetry);
        this.intake = new Intake(hardwareMap, operatorGamepad, telemetry);
        this.shooter = new Shooter(hardwareMap, operatorGamepad, telemetry);

        this.commandFactory = new CommandFactory(telemetry, null, drive, intake, shooter);

        register(drive);
//        register(drive, intake, shooter);
    }
}
