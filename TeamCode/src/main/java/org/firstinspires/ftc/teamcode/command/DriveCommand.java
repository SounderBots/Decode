package org.firstinspires.ftc.teamcode.command;

import android.util.Log;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.teamcode.pedroPathing.DrawingToPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriveCommand extends SounderBotCommandBase {

    private static final String LOG_TAG = DriveCommand.class.getSimpleName();
    final Follower follower;
    PathChain pathChain;
    boolean following = false;

    private final Pose start;
    private final Pose end;
    private final boolean isFirstMove;

    private double tempMaxPower = -1;

    private List<Pose> points;

    public static long DEFAULT_TIMEOUT_IN_SECONDS = 4;
    private final PathType pathType;

    public DriveCommand(Follower follower, @NonNull Pose end, PathType pathType, boolean isFirstMove) {
        this(follower, List.of(new Pose(0, 0, 0), end), pathType, DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, isFirstMove);
    }

    public DriveCommand(Follower follower, @NonNull Pose start, @NonNull Pose end, PathType pathType, boolean isFirstMove) {
        this(follower, List.of(start, end), pathType, DEFAULT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, isFirstMove);
    }
    public DriveCommand(Follower follower, List<Pose> points, PathType pathType, long timeOut, TimeUnit timeUnit, boolean isFirstMove) {
        super(TimeUnit.MILLISECONDS.convert(timeOut, timeUnit));

        this.follower = follower;
        this.isFirstMove = isFirstMove;
        this.start = points.get(0);
        this.end = points.get(points.size() - 1);
        this.points = points;
        this.pathType = pathType;
    }

    public DriveCommand withTempMaxPower(double tempMaxPower) {
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
                    .addPath(getCurve(startPos))
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

    protected BezierCurve getCurve(Pose startPos) {
        return switch (pathType) {
            case LINE -> new BezierLine(startPos, end);
            case CURVE -> {
                List<Pose> controlPoints = new ArrayList<>(points.size() + 1);
                controlPoints.add(startPos);
                controlPoints.addAll(points);
                yield new BezierCurve(controlPoints);
            }
        };
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
