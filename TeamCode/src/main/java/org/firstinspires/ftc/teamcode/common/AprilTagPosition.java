package org.firstinspires.ftc.teamcode.common;

public record AprilTagPosition (
        AprilTagEnum aprilTag,
        double distance,
        double horizontalAngle,
        double verticalAngle){
    public AprilTagPosition(AprilTagEnum aprilTag) {
        this(aprilTag, 0.0d, 0.0d, 0.0d);
    }
}
