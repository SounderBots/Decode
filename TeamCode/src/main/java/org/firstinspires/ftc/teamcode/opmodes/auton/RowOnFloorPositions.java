package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public interface RowOnFloorPositions {
    public Pose getFirstRowStartPosition();
    public Pose getFirstRowEndPosition();
    public Pose getSecondRowStartPosition();
    public Pose getSecondRowEndPosition();
    public Pose getThirdRowStartPosition();
    public Pose getThirdRowEndPosition();
}
