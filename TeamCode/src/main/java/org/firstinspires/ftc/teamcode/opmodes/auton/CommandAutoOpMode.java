package org.firstinspires.ftc.teamcode.opmodes.auton;

//import com.acmerobotics.dashboard.FtcDashboard;
//import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.bylazar.telemetry.PanelsTelemetry;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.command.CommandFactory;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.subsystems.drivetrain.AutonDriveTrain;
import org.firstinspires.ftc.teamcode.subsystems.feedback.RGBLightIndicator;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Intake;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.scoring.Stopper;
import org.firstinspires.ftc.teamcode.subsystems.scoring.TransferChamber;
import org.firstinspires.ftc.teamcode.subsystems.vision.LimeLightAlign;
import org.firstinspires.ftc.teamcode.util.DelegateOrVoidTelemetry;

public abstract class CommandAutoOpMode extends CommandOpMode {

    protected CommandFactory commandFactory;

    boolean barebone = false;

    private static final boolean emitTelemetry = true;

    Command finalGroup;

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void waitForStart() {
        super.waitForStart();
        schedule(finalGroup);
        logInitStep("Commands scheduled");
    }

    @Override
    public void initialize() {
        logInitStep("Beginning");
        telemetry = new DelegateOrVoidTelemetry(new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry(), PanelsTelemetry.INSTANCE.getFtcTelemetry()), emitTelemetry);
        GamepadEx driverGamePad = new GamepadEx(gamepad1);
        GamepadEx operatorGamePad = new GamepadEx(gamepad2);
        logInitStep("telemetry, gamepads created");
        AutonDriveTrain driveTrain = new AutonDriveTrain(hardwareMap, telemetry);
        TransferChamber transferChamber = new TransferChamber(hardwareMap, operatorGamePad, telemetry) {
            @Override
            public void periodic() {
                // do nothing
            }
        };
        Intake intake = new Intake(hardwareMap, operatorGamePad, telemetry);
        RGBLightIndicator rgbLightIndicator = new RGBLightIndicator(hardwareMap, telemetry, "RGBIndicator");
        Stopper stopper = new Stopper(hardwareMap, operatorGamePad, telemetry);
        Follower follower = Constants.createFollower(hardwareMap);
        LimeLightAlign limeLightAlign = new LimeLightAlign(hardwareMap, telemetry)
                .withHeadingSupplier(() -> {
                    Pose followerPose = follower.getPose();
//                    Log.i(AutonCommonConfigs.LOG_TAG, "follower reported pose: " + followerPose);
                    return followerPose;
                });
        Shooter shooter = new Shooter(hardwareMap, operatorGamePad, telemetry, rgbLightIndicator, limeLightAlign, this.getClass().getSimpleName());
        shooter.AutoSpeedAndTilt();
        logInitStep("all subsystems created");


        commandFactory = new CommandFactory(telemetry, driveTrain, follower, null, intake, shooter, transferChamber, stopper, limeLightAlign);

        logInitStep("command factory created");
        logInitStep("before setting intake");

        logInitStep("After setting intake");

        // sleep 30s after createCommand is a fill gap command to avoid IndexOutOfBoundException
//        finalGroup = new SounderBotParallelRaceGroup(
//                commandFactory.sleep(3200000),
//                createCommand().andThen(
//                        commandFactory.sleep(3000000)
//                ));
        finalGroup = createCommand();
        logInitStep("Commands created");
    }

    protected abstract Command createCommand();

    private void logInitStep(String step) {
//        Log.i(LOG_TAG, "Init: " + step);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        try {
            super.runOpMode();
        } catch (InterruptedException e) {
            //do nothing
        } finally {
            if (commandFactory != null && commandFactory.getShooter() != null) {
                commandFactory.getShooter().stopLogging();
            }
        }
    }
}
