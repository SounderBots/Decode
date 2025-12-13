package org.firstinspires.ftc.teamcode.opmodes.auton.positions;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.geometry.Pose;

@Configurable
public interface Positions {
    public Pose getGPPStartPosition();
    public Pose getGPPEndPosition();
    public Pose getPGPStartPosition();
    public Pose getPGPEndPosition();
    public Pose getPPGStartPosition();
    public Pose getPPGEndPosition();

    public Pose getShortStartPosition();
    public Pose getLongStartPosition();

    public Pose getShortFinishPosition();
    public Pose getLongFinishPosition();

    public Pose getLongShootPosition();
    public Pose getShortShootPosition();

    public Pose getObeliskObservePosition();

    default double getDriveTrainIntakePowerScale() {
        return 1;
    }

    default Pose getLongPreloadShootPosition() {
        return getLongShootPosition();
    }

    default Pose getShortPreloadShootPosition() {
        return getShortShootPosition();
    }

    default boolean openGateBetweenPPGAndPGP() {
        return false;
    }

    default boolean observeObelisk() {
        return true;
    }

    default double getOpenGateHeadingDegrees() {
        return 0;
    }
}
