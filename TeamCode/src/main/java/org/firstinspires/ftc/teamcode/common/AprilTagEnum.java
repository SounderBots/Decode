package org.firstinspires.ftc.teamcode.common;

import android.util.Log;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;

import java.util.EnumSet;
import java.util.Optional;

public enum AprilTagEnum {
    BLUE_GOAL(20),
    OBELISK_GPP(21),
    OBELISK_PGP(22),
    OBELISK_PPG(23),
    RED_GOAL(24),
    UNKNOWN(0)
    ;

    private static final String LOG_TAG = AprilTagEnum.class.getSimpleName();

    public static final EnumSet<AprilTagEnum> OBELISK_ALL = EnumSet.of(OBELISK_GPP, OBELISK_PGP, OBELISK_PPG);

    private final int value;

    AprilTagEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AprilTagEnum fromValue(int value) {
        for (AprilTagEnum tag : AprilTagEnum.values()) {
            if (tag.getValue() == value) {
                return tag;
            }
        }
        Log.w(LOG_TAG, "Invalid AprilTag value: " + value);
        return AprilTagEnum.UNKNOWN;
    }

    public Optional<RowsOnFloor> toRowsOnFloor() {
        return RowsOnFloor.fromAprilTagId(this.getValue());
    }

    public static boolean isValidObeliskTag(int value) {
        return OBELISK_ALL.contains(fromValue(value));
    }
}
