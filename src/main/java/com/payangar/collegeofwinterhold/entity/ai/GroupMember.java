package com.payangar.collegeofwinterhold.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Shared leader/follower contract implemented by any mob that can belong
 * to a group (vampire covens, wizard exploration parties, etc.). Used by
 * {@link FollowLeaderGoal} and {@link CopyLeaderTargetGoal} so the same
 * goal classes work across entity families.
 */
public interface GroupMember {

    /** {@code true} if this entity is the leader of its group. Leaders never follow. */
    boolean isGroupLeader();

    /**
     * Resolves the leader entity in the current level. Returns {@code null}
     * if this entity has no leader, the leader is unloaded or dead, or we
     * are not on a server. Server-side only.
     */
    @Nullable
    LivingEntity getGroupLeader();
}
