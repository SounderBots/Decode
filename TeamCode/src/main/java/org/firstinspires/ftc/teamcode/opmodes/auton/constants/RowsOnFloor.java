package org.firstinspires.ftc.teamcode.opmodes.auton.constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;

public enum RowsOnFloor {
    GPP(21), // the row close to final parking position
    PGP(22),
    PPG(23),
    NONE (0),
    NOT_TRIED (-1);

    static final Map<Integer, RowsOnFloor> aprilTagIdToRow = new HashMap<>();

    @Getter
    final int aprilTagId;
    RowsOnFloor(int aprilTagId) {
        this.aprilTagId = aprilTagId;
    }

    public static Optional<RowsOnFloor> fromAprilTagId(int aprilTagId) {
        if (aprilTagIdToRow.isEmpty()) {
            for (RowsOnFloor row : RowsOnFloor.values()) {
                aprilTagIdToRow.put(row.aprilTagId, row);
            }
        }
        return Optional.ofNullable(aprilTagIdToRow.get(aprilTagId));
    }
}
