package com.payangar.collegeofwinterhold.entity.ai;

import com.payangar.collegeofwinterhold.entity.vampire.VampireEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Pulls a coven follower back to its leader when the distance between them
 * exceeds {@code maxDistance}. Yields cleanly when the leader is missing or
 * dead so the follower falls back to its own AI chain (patrol, wander, etc.).
 *
 * <p>Leader-marked vampires short-circuit this goal — the leader drives its
 * own movement and never follows anyone.
 */
public final class FollowLeaderGoal extends Goal {
    private final VampireEntity follower;
    private final double speed;
    private final float minDistSqr;
    private final float maxDistSqr;
    private @Nullable LivingEntity leader;
    private int pathTimer;

    public FollowLeaderGoal(VampireEntity follower, double speed, float minDistance, float maxDistance) {
        this.follower = follower;
        this.speed = speed;
        this.minDistSqr = minDistance * minDistance;
        this.maxDistSqr = maxDistance * maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (follower.isCovenLeader()) return false;
        LivingEntity l = follower.getLeader();
        if (l == null) return false;
        if (follower.distanceToSqr(l) < maxDistSqr) return false;
        this.leader = l;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (leader == null || !leader.isAlive()) return false;
        return follower.distanceToSqr(leader) > minDistSqr;
    }

    @Override
    public void start() {
        pathTimer = 0;
    }

    @Override
    public void stop() {
        leader = null;
        follower.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (leader == null) return;
        follower.getLookControl().setLookAt(leader, 10.0f, follower.getMaxHeadXRot());
        if (--pathTimer <= 0) {
            pathTimer = 10;
            follower.getNavigation().moveTo(leader, speed);
        }
    }
}
