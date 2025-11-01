package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriveToTargetPedroPathCommand extends SounderBotCommandBase {

    private static final String LOG_TAG = DriveToTargetPedroPathCommand.class.getSimpleName();
    final Follower follower;
    PathChain pathChain;
    boolean following = false;

    public DriveToTargetPedroPathCommand(Follower follower, @NonNull Pose start, @NonNull Pose end) {
        this(follower, start, end, 2, TimeUnit.SECONDS);
    }
    public DriveToTargetPedroPathCommand(Follower follower, @NonNull Pose start, @NonNull Pose end, long timeOut,  TimeUnit timeUnit) {
        super(TimeUnit.MILLISECONDS.convert(timeOut, timeUnit));

        Log.i(LOG_TAG, "start: " + start);
        Log.i(LOG_TAG, "end: " + end);

        this.follower = follower;
        this.follower.setStartingPose(start);
        pathChain = follower.pathBuilder()
                .addPath(new BezierLine(start, end))
                .setGlobalLinearHeadingInterpolation(start.getHeading(), end.getHeading())
                .build();
    }

    @Override
    public void initialize() {
        super.initialize();
        following = false;
        follower.activateAllPIDFs();
    }

    @Override
    protected void doExecute() {
        follower.update();
        if (!following) {
            following = true;
            follower.followPath(pathChain);
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
        follower.deactivateAllPIDFs();
    }
}
