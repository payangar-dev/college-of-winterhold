package com.payangar.collegeofwinterhold.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Mirrors the leader's current target onto the follower. Yields when the
 * leader is missing or dead so the follower falls back to its own target
 * goals (HurtByTargetGoal, NearestAttackableTargetGoal, ...).
 */
public final class CopyLeaderTargetGoal<T extends Mob & GroupMember> extends Goal {
    private final T follower;
    private @Nullable LivingEntity seenTarget;

    public CopyLeaderTargetGoal(T follower) {
        this.follower = follower;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (follower.isGroupLeader()) return false;
        LivingEntity leader = follower.getGroupLeader();
        if (!(leader instanceof Mob leaderMob)) return false;
        LivingEntity leaderTarget = leaderMob.getTarget();
        if (leaderTarget == null || !leaderTarget.isAlive()) return false;
        if (!follower.canAttack(leaderTarget)) return false;
        this.seenTarget = leaderTarget;
        return true;
    }

    @Override
    public void start() {
        follower.setTarget(seenTarget);
        super.start();
    }

    @Override
    public void stop() {
        seenTarget = null;
        super.stop();
    }
}
