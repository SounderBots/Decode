package org.firstinspires.ftc.teamcode.common;

public record BallPosition(
        double distance,
        double horizontalAngle,
        double verticalAngle){
    public BallPosition() {
        this(0.0d, 0.0d, 0.0d);
    }
}
