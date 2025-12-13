package org.firstinspires.ftc.teamcode.opmodes.auton;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auton.positions.BlueShortPositions;
import org.firstinspires.ftc.teamcode.opmodes.auton.positions.Positions;

@Autonomous(name="Blue shoot from front and open the gate", group="Blue")
@Configurable
public class BlueShootFromFrontOpenGate extends BlueShootFromFront {

    @Override
    public Positions getPositions() {

        return new BlueShortPositions() {

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
