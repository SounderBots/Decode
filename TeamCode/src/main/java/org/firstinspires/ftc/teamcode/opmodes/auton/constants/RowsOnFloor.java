package org.firstinspires.ftc.teamcode.opmodes.auton.constants;

import lombok.Getter;

public enum RowsOnFloor {
    GPP(21), // the row close to final parking position
    PGP(22),
    PPG(23);

    @Getter
    final int aprilTagId;
    RowsOnFloor(int aprilTagId) {
        this.aprilTagId = aprilTagId;
    }
}
