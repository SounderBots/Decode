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

    public Pose getFrontStartPosition();
    public Pose getBackStartPosition();

    public Pose getFrontFinishPosition();
    public Pose getBackFinishPosition();

    public Pose getBackShootPosition();
    public Pose getFrontShootPosition();
}
