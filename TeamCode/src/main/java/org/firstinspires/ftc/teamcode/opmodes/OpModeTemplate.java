package org.firstinspires.ftc.teamcode.opmodes;

import android.util.Log;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.gamepad.GamepadEx;

import org.firstinspires.ftc.teamcode.util.DelegateOrVoidTelemetry;

public abstract class OpModeTemplate extends CommandOpMode {

    private static final String LOG_TAG = "AUTO_DEBUG";
    protected GamepadEx driverGamepad;
    protected GamepadEx operatorGamepad;

    private static final boolean emitTelemetry = true;

    @Override
    public void initialize() {
        logInitStep("beginning");
        driverGamepad = new GamepadEx(gamepad1);
        operatorGamepad = new GamepadEx(gamepad2);
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new DelegateOrVoidTelemetry(new MultipleTelemetry(telemetry, dashboard.getTelemetry()), emitTelemetry);
        logInitStep("telemetry created");
        dashboard.setTelemetryTransmissionInterval(200);
    }

    private void logInitStep(String step) {
        Log.i(LOG_TAG, "Init: " + step);
    }
}
