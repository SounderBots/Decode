package org.firstinspires.ftc.teamcode.command;

import org.firstinspires.ftc.teamcode.opmodes.auton.constants.RowsOnFloor;
import org.firstinspires.ftc.teamcode.opmodes.auton.constants.ShootRange;

import java.util.List;

import lombok.Getter;

public class CommandRuntimeSharedProperties {
    @Getter
    public static volatile RowsOnFloor observedObeliskShowedRow = RowsOnFloor.NOT_TRIED;

    @Getter
    public static volatile List<RowsOnFloor> rowSequence = List.of();

    public static List<RowsOnFloor> computeRowSequence(ShootRange range) {
        rowSequence = switch (observedObeliskShowedRow) {
            case NONE, NOT_TRIED -> switch (range) {
                case SHORT -> List.of(RowsOnFloor.PPG, RowsOnFloor.PGP);
                case LONG -> List.of(RowsOnFloor.GPP);
            };
            case GPP -> switch (range) {
                case SHORT -> List.of(RowsOnFloor.GPP, RowsOnFloor.PGP);
                case LONG -> List.of(RowsOnFloor.GPP);
            };
            case PGP -> switch (range) {
                case SHORT -> List.of(RowsOnFloor.PGP, RowsOnFloor.PPG);
                case LONG -> List.of(RowsOnFloor.PGP);
            };
            case PPG -> switch (range) {
                case SHORT -> List.of(RowsOnFloor.PPG, RowsOnFloor.PGP);
                case LONG -> List.of(RowsOnFloor.PPG);
            };
        };
        return rowSequence;
    }
}
