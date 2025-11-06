package org.firstinspires.ftc.teamcode.subsystems.feedback;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class RGBLightIndicator extends SubsystemBase {

    Servo rgbIndicator;
    Telemetry telemetry;
    String configString;
    private static Double OFF_COL = 0.0;

    @Config
    public static class LightConfig {
        public static double GreenColor = 0.5;

        public static double RedColor = .3;
    }

    public RGBLightIndicator(HardwareMap hardwareMap, Telemetry telemetry, String configString) {
        this.telemetry = telemetry;
        this.configString = configString;
        rgbIndicator = hardwareMap.get(Servo.class, configString);
        if (rgbIndicator.getController() instanceof PwmControl) {
            ((PwmControl) rgbIndicator.getController()).setPwmRange(new PwmControl.PwmRange(500, 2500));
        }
    }

    public void changeGreen(){
        setColor(LightConfig.GreenColor);
    }

    public void changeRed(){
        setColor(LightConfig.RedColor);
    }

    public void changeOff(){
        setColor(OFF_COL);
    }

    /**
     * Sets the color of the RGB Indicator using PWM values.
     * @param position The position value (0.0 to 1.0) corresponding to the desired color.
     */
    private void setColor(double position) {
        rgbIndicator.setPosition(position);
        telemetry.update();
    }

}

