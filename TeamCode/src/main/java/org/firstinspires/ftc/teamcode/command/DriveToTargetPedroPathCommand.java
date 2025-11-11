package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.teamcode.pedroPathing.DrawingToPanel;

import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriveToTargetPedroPathCommand extends SounderBotCommandBase {

    private static final String LOG_TAG = DriveToTargetPedroPathCommand.class.getSimpleName();
    final Follower follower;
    PathChain pathChain;
    boolean following = false;

    private final Pose start;
    private final Pose end;
    private final boolean isFirstMove;

    private double tempMaxPower = -1;

    public DriveToTargetPedroPathCommand(Follower follower, @NonNull Pose end, boolean isFirstMove) {
        this(follower, new Pose(0, 0, 0), end, 4, TimeUnit.SECONDS, isFirstMove);
    }

    public DriveToTargetPedroPathCommand(Follower follower, @NonNull Pose start, @NonNull Pose end, boolean isFirstMove) {
        this(follower, start, end, 4, TimeUnit.SECONDS, isFirstMove);
    }
    public DriveToTargetPedroPathCommand(Follower follower, @NonNull Pose start, @NonNull Pose end, long timeOut,  TimeUnit timeUnit, boolean isFirstMove) {
        super(TimeUnit.MILLISECONDS.convert(timeOut, timeUnit));

        this.follower = follower;
        this.isFirstMove = isFirstMove;
        this.start = start;
        this.end = end;
    }

    public DriveToTargetPedroPathCommand withTempMaxPower(double tempMaxPower) {
        this.tempMaxPower = tempMaxPower;
        return this;
    }

    @Override
    public void initialize() {
        super.initialize();
        following = false;
        follower.activateAllPIDFs();
        DrawingToPanel.init();
    }

    @Override
    protected void doExecute() {
        Pose startPos = start;
        if (!following) {
            if (isFirstMove) {
                Log.i(LOG_TAG, "Starting position = " + start);
                this.follower.setStartingPose(start);
            } else {
                startPos = follower.getPose();
            }

            pathChain = follower.pathBuilder()
                    .addPath(new BezierLine(startPos, end))
                    .setLinearHeadingInterpolation(startPos.getHeading(), end.getHeading())
                    .build();
            if (tempMaxPower > 0) {
                follower.setMaxPower(tempMaxPower);
            }
            follower.followPath(pathChain);
            following = true;
        } else {
            follower.update();
            DrawingToPanel.drawDebug(follower);
        }

    }

    @Override
    protected boolean isTargetReached() {
        return following && !(follower.isBusy());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        following = false;
        follower.breakFollowing();
        follower.setMaxPower(1);
        tempMaxPower = -1;
        follower.deactivateAllPIDFs();
    }
}
