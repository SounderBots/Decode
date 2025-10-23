package org.firstinspires.ftc.teamcode.opmodes.auton;

//import com.acmerobotics.dashboard.FtcDashboard;
//import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.gamepad.GamepadEx;

import org.firstinspires.ftc.teamcode.command.CommandFactory;
import org.firstinspires.ftc.teamcode.subsystems.AutonDriveTrain;
import org.firstinspires.ftc.teamcode.util.DelegateOrVoidTelemetry;

public abstract class CommandAutoOpMode extends CommandOpMode {

    protected CommandFactory commandFactory;

    boolean barebone = false;

    private static final boolean emitTelemetry = true;

    @Override
    public void reset() {
        super.reset();
    }

    private static final String LOG_TAG = "AUTO_DEBUG";
    @Override
    public void initialize() {
        logInitStep("Beginning");
        telemetry = new DelegateOrVoidTelemetry(telemetry, emitTelemetry);
        AutonDriveTrain driveTrain = new AutonDriveTrain(hardwareMap, telemetry);

//        GamepadEx driverGamePad = new GamepadEx(gamepad1);
//        GamepadEx operatorGamePad = new GamepadEx(gamepad2);

        logInitStep("telemetry, gamepads created");

//        DriverFeedback feedback = barebone ? null : new DriverFeedback(hardwareMap, driverGamePad, operatorGamePad, telemetry);
//
//        //LimeLight limeLight = barebone ? null : new LimeLight(hardwareMap, telemetry);
//        DriveTrain driveTrain = new DriveTrain(hardwareMap, driverGamePad, telemetry, null, null);
//        MultiAxisIntake intake = barebone ? null : new MultiAxisIntake(hardwareMap, operatorGamePad, telemetry, feedback);
//        DeliveryPivot pivot = barebone ? null : new DeliveryPivot(hardwareMap, operatorGamePad, telemetry, feedback, null);
//        DeliverySlider slider = barebone ? null : new DeliverySlider(hardwareMap, operatorGamePad, telemetry, feedback, pivot);
////        SpecimenSlider specimenSlider = barebone ? null : new SpecimenSlider(hardwareMap, telemetry, feedback);
////        SpecimenSliderClaw  specimenSliderClaw = barebone ? null : new SpecimenSliderClaw(hardwareMap, telemetry, feedback);
//        SampleSweeper sampleSweeper = new SampleSweeper(hardwareMap, operatorGamePad, telemetry, feedback);
//        Bumper bumper = new Bumper(hardwareMap);
        logInitStep("all subsystems created");
        commandFactory = new CommandFactory(telemetry, driveTrain, null, null, null);

        logInitStep("command factory created");
        logInitStep("before setting intake");

        logInitStep("After setting intake");

        // sleep 30s after createCommand is a fill gap command to avoid IndexOutOfBoundException
        Command finalGroup = new ParallelRaceGroup(
                commandFactory.sleep(32000),
                createCommand().andThen(
                        commandFactory.sleep(30000)
                ));
        logInitStep("Commands created");
        schedule(finalGroup);
        logInitStep("Commands scheduled");
    }

    protected abstract Command createCommand();

    private void logInitStep(String step) {
//        Log.i(LOG_TAG, "Init: " + step);
    }
}
