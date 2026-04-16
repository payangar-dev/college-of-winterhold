package com.payangar.collegeofwinterhold.entity.villager;

import com.payangar.collegeofwinterhold.registry.ModAttachments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Highest-priority goal injected on vanilla {@link Villager} entities marked
 * as captured by a vampire coven jailer. Produces panic sounds, erratic micro-
 * movements within the leash range, and blocks all normal activity (trading,
 * sleeping, socialising). Removed when the villager is freed (transition to
 * {@code rescued} state).
 *
 * <p>Also enforces the "unbreakable leash" contract by clamping the villager's
 * distance to its leash holder before the vanilla {@code tickLeash} check runs
 * (goal ticking is called from {@code serverAiStep} inside {@code super.tick()},
 * whereas {@code tickLeash} runs after it in {@code Mob.tick()}).
 */
public final class CapturedPanicGoal extends Goal {

    private static final double LEASH_CLAMP_DIST_SQR = 8.0 * 8.0;

    private final Villager villager;
    private int soundCooldown;

    public CapturedPanicGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return villager.hasData(ModAttachments.CAPTURED_STATE)
                && villager.getData(ModAttachments.CAPTURED_STATE).isCaptured();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        // Leash clamp — runs BEFORE vanilla tickLeash() in the tick pipeline.
        // Keeps the villager within 8 blocks of its holder so the vanilla
        // 10-block break threshold is never reached.
        Entity holder = villager.getLeashHolder();
        if (holder != null && villager.distanceToSqr(holder) > LEASH_CLAMP_DIST_SQR) {
            Vec3 toHolder = holder.position().subtract(villager.position()).normalize().scale(2.0);
            villager.teleportTo(
                    holder.getX() - toHolder.x,
                    holder.getY(),
                    holder.getZ() - toHolder.z
            );
        }

        // Erratic micro-movements : small random offset every ~20 ticks.
        if (villager.tickCount % 20 == 0) {
            double dx = (villager.getRandom().nextDouble() - 0.5) * 1.5;
            double dz = (villager.getRandom().nextDouble() - 0.5) * 1.5;
            Vec3 target = villager.position().add(dx, 0, dz);
            villager.getNavigation().moveTo(target.x, target.y, target.z, 0.5);
        }

        // Periodic panic sounds.
        if (--soundCooldown <= 0) {
            boolean cry = villager.getRandom().nextFloat() < 0.3f;
            villager.playSound(cry ? SoundEvents.VILLAGER_NO : SoundEvents.VILLAGER_HURT,
                    0.6f, 0.9f + villager.getRandom().nextFloat() * 0.2f);
            soundCooldown = 40 + villager.getRandom().nextInt(60);
        }
    }

    @Override
    public void start() {
        soundCooldown = 0;
        villager.getNavigation().stop();
    }

    @Override
    public void stop() {
        villager.getNavigation().stop();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
