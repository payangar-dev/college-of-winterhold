package com.payangar.collegeofwinterhold.entity.villager;

import com.payangar.collegeofwinterhold.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Runs on rescued villagers (after their jailer has been killed). Checks
 * three safety conditions every 10 ticks for {@value #SAFE_TICKS_REQUIRED}
 * consecutive ticks (= 10 seconds of uninterrupted safety). When met,
 * triggers the reward delivery and returns the villager to normal life.
 *
 * <p>Safety conditions :
 * <ol>
 *   <li>No hostile mob within {@value #CHECK_RADIUS} blocks.</li>
 *   <li>No mob is currently targeting the villager (within 32 blocks).</li>
 *   <li>At least one player within {@value #CHECK_RADIUS} blocks.</li>
 * </ol>
 *
 * <p>Injected at capture time alongside {@link CapturedPanicGoal}. Both
 * goals live permanently in the villager's goal selector and activate based
 * on the {@link CapturedState} lifecycle : {@code captured → rescued → clear}.
 */
public final class RescuedSafetyGoal extends Goal {

    private static final double CHECK_RADIUS = 16.0;
    private static final int SAFE_TICKS_REQUIRED = 20; // checked every 10 ticks → 20 × 10 = 200 ticks = 10 s
    private static final int CHECK_INTERVAL = 10;

    private final Villager villager;

    public RescuedSafetyGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.noneOf(Flag.class)); // doesn't block movement
    }

    @Override
    public boolean canUse() {
        return villager.hasData(ModAttachments.CAPTURED_STATE)
                && villager.getData(ModAttachments.CAPTURED_STATE).isRescued();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (villager.tickCount % CHECK_INTERVAL != 0) return;

        CapturedState state = villager.getData(ModAttachments.CAPTURED_STATE);
        AABB checkBox = new AABB(villager.blockPosition()).inflate(CHECK_RADIUS);

        boolean hostileNearby = !villager.level().getEntitiesOfClass(
                Mob.class, checkBox,
                m -> m instanceof Enemy && m.isAlive()
        ).isEmpty();

        boolean targeted = !villager.level().getEntitiesOfClass(
                Mob.class, checkBox.inflate(16),
                m -> m.getTarget() == villager
        ).isEmpty();

        Player nearestPlayer = villager.level().getNearestPlayer(villager, CHECK_RADIUS);
        boolean playerPresent = nearestPlayer != null;

        if (!hostileNearby && !targeted && playerPresent) {
            state.incrementSafeTicks();
        } else {
            state.resetSafeTicks();
        }

        if (state.safeTicks() >= SAFE_TICKS_REQUIRED) {
            deliverReward();
            state.clear();
        }
    }

    /**
     * Reward delivery — gossip bump on the villager for the nearest player,
     * plus a small bundle of items thrown toward them. Mirrors the vanilla
     * raid hero reward in spirit (reputation + loot), scaled down to a
     * single-villager rescue rather than a full village defense.
     */
    private void deliverReward() {
        Player nearest = villager.level().getNearestPlayer(villager, CHECK_RADIUS);
        if (nearest == null) return;

        // Gossip : MAJOR_POSITIVE 25 — gives a notable price reduction from
        // this specific villager to the rescuer, roughly half a zombie-cure.
        villager.getGossips().add(
                nearest.getUUID(),
                net.minecraft.world.entity.ai.gossip.GossipType.MAJOR_POSITIVE,
                25
        );

        // Items : independent rolls, all thrown toward the rescuer.
        RandomSource rng = villager.getRandom();
        throwIfRolled(rng, nearest, Items.EMERALD,    1.00f, 1, 3);
        throwIfRolled(rng, nearest, Items.BREAD,      0.30f, 1, 2);
        throwIfRolled(rng, nearest, Items.IRON_INGOT, 0.04f, 1, 1);
        throwIfRolled(rng, nearest, Items.GOLD_INGOT, 0.02f, 1, 1);
        throwIfRolled(rng, nearest, Items.DIAMOND,    0.005f, 1, 1);

        villager.gameEvent(GameEvent.ENTITY_INTERACT);
    }

    /**
     * Rolls a drop and, if successful, throws the item toward the given
     * player with a gentle arc — same visual feel as the vanilla villager
     * gift throw after a raid victory.
     */
    private void throwIfRolled(RandomSource rng, Player target,
                               net.minecraft.world.item.Item item,
                               float chance, int min, int max) {
        if (rng.nextFloat() >= chance) return;
        int count = min + (max > min ? rng.nextInt(max - min + 1) : 0);
        ItemStack stack = new ItemStack(item, count);

        Vec3 toward = target.position().subtract(villager.position()).normalize();
        double vx = toward.x * 0.3 + rng.nextGaussian() * 0.02;
        double vy = 0.3 + rng.nextGaussian() * 0.02;
        double vz = toward.z * 0.3 + rng.nextGaussian() * 0.02;

        ItemEntity drop = new ItemEntity(
                villager.level(),
                villager.getX(), villager.getEyeY() - 0.3, villager.getZ(),
                stack, vx, vy, vz
        );
        drop.setDefaultPickUpDelay();
        villager.level().addFreshEntity(drop);
    }
}
