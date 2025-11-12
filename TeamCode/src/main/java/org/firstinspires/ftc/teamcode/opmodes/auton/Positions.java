package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public interface Positions {
    public Pose getFirstRowStartPosition();
    public Pose getFirstRowEndPosition();
    public Pose getSecondRowStartPosition();
    public Pose getSecondRowEndPosition();
    public Pose getThirdRowStartPosition();
    public Pose getThirdRowEndPosition();

    public Pose getShortStartPosition();
    public Pose getLongStartPosition();

    public Pose getShortFinishPosition();
    public Pose getLongFinishPosition();

    public Pose getLongShootPosition();
    public Pose getShortShootPosition();
}
