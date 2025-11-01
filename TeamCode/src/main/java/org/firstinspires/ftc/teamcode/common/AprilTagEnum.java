package org.firstinspires.ftc.teamcode.common;

import java.util.EnumSet;

public enum AprilTagEnum {
    BLUE_GOAL(20),
    OBELISK_GPP(21),
    OBELISK_PGP(22),
    OBELISK_PPG(23),
    RED_GOAL(24)
    ;

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
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
