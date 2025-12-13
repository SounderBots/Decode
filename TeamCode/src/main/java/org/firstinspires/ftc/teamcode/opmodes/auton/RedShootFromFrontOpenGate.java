package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.RedShortPositions;

@Autonomous(name="Red shoot from front and open the gate", group="Red")
@Configurable
public class RedShootFromFrontOpenGate extends RedShootFromFront {

    @Override
    public Positions getPositions() {
        return new RedShortPositions() {
            @Override
            public boolean observeObelisk() {
                return false;
            }

            @Override
            public boolean openGateBetweenPPGAndPGP() {
                return true;
            }
        };
    }
}
