package com.payangar.collegeofwinterhold.world;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.config.ModServerConfig;
import com.payangar.collegeofwinterhold.entity.vampire.VampireEntity;
import com.payangar.collegeofwinterhold.entity.vampire.core.VampireBiomeTags;
import com.payangar.collegeofwinterhold.entity.vampire.core.VampireVariant;
import com.payangar.collegeofwinterhold.entity.villager.CapturedPanicGoal;
import com.payangar.collegeofwinterhold.entity.villager.RescuedSafetyGoal;
import com.payangar.collegeofwinterhold.registry.ModAttachments;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

/**
 * Player-centric radial vampire coven spawner.
 *
 * <p>Covens only materialise at night — vampires burn in daylight. Biome is
 * gated via {@link VampireBiomeTags#VAMPIRE_COVEN_BIOMES}. Ground resolution
 * is the shared {@link AbstractGroupSpawner#walkDownToSurface} — it already
 * skips logs and leaves, which is exactly what a dense dark-forest canopy
 * needs.
 *
 * <p>See {@link AbstractGroupSpawner} for the shared spawning algorithm
 * (directional cone, adaptive attempts, chunk safety, spacing gate).
 */
public class VampireCovenSpawner extends AbstractGroupSpawner {

    public static final VampireCovenSpawner INSTANCE = new VampireCovenSpawner();

    VampireCovenSpawner() {}

    // ── Config hooks ────────────────────────────────────────────────────────

    @Override protected int scanIntervalTicks() { return ModServerConfig.COVEN_SCAN_INTERVAL_TICKS.get(); }
    @Override protected int attemptsPerScan()    { return ModServerConfig.COVEN_ATTEMPTS_PER_SCAN.get(); }
    @Override protected int minSpawnDistance()    { return ModServerConfig.COVEN_MIN_SPAWN_DISTANCE.get(); }
    @Override protected int maxSpawnDistance()    { return ModServerConfig.COVEN_MAX_SPAWN_DISTANCE.get(); }
    @Override protected int minSpacingBlocks()    { return ModServerConfig.COVEN_MIN_SPACING_BLOCKS.get(); }
    @Override protected double forwardBias()      { return ModServerConfig.COVEN_FORWARD_BIAS.get(); }

    // ── Behavioral hooks ────────────────────────────────────────────────────

    @Override
    protected boolean canSpawnNow(ServerLevel server) {
        return server.isNight();
    }

    @Override
    protected double spawnChance(ServerLevel server, BlockPos ground) {
        return ModServerConfig.COVEN_SPAWN_CHANCE.get();
    }

    @Override
    protected boolean isValidBiome(ServerLevel server, BlockPos probe) {
        return server.getBiome(probe).is(VampireBiomeTags.VAMPIRE_COVEN_BIOMES);
    }

    @Override
    protected boolean isSpacingClear(ServerLevel server, BlockPos ground) {
        AABB box = new AABB(ground).inflate(minSpacingBlocks());
        return server.getEntitiesOfClass(VampireEntity.class, box).isEmpty();
    }

    @Override
    protected void spawnGroup(ServerLevel server, BlockPos pos, RandomSource rng) {
        spawnCoven(server, pos, rng);
    }

    // ── Group composition (public static for debug command) ─────────────────

    /**
     * Spawns a coven at the given ground position. Exposed for the debug
     * command ({@code /cow spawn_coven}) and for the spawner above.
     * Expects {@code center} to already be a valid walkable ground spot.
     */
    public static void spawnCoven(ServerLevel server, BlockPos center, RandomSource rng) {
        int count = 3 + rng.nextInt(3); // 3, 4 or 5
        VampireEntity leader = null;
        VampireVariant leaderVariantLog = null;
        var followers = new ArrayList<VampireEntity>();

        for (int i = 0; i < count; i++) {
            int dx = rng.nextInt(7) - 3;
            int dz = rng.nextInt(7) - 3;
            BlockPos foot = AbstractGroupSpawner.walkDownToSurface(
                    server, center.getX() + dx, center.getZ() + dz);
            if (foot == null) foot = center;

            VampireEntity vampire = ModEntityTypes.VAMPIRE.get().create(server);
            if (vampire == null) continue;

            vampire.moveTo(foot.getX() + 0.5, foot.getY(), foot.getZ() + 0.5,
                    rng.nextFloat() * 360f - 180f, 0f);

            if (leader == null) {
                VampireVariant leaderVariant = rng.nextFloat() < 0.70f
                        ? VampireVariant.VAMPIRIC_BOOK
                        : VampireVariant.NECRONOMICON;
                vampire.setForcedVariant(leaderVariant);
                vampire.setCovenLeader(true);
                leader = vampire;
                leaderVariantLog = leaderVariant;
            } else {
                vampire.setLeaderUuid(leader.getUUID());
                followers.add(vampire);
            }

            vampire.finalizeSpawn(server, server.getCurrentDifficultyAt(foot),
                    MobSpawnType.STRUCTURE, null);
            server.addFreshEntityWithPassengers(vampire);
            SpawnDebugBroadcaster.glow(vampire);
        }

        // Jailer + captives : 30% of covens have a jailer. When present,
        // always 1 captive villager, 30% chance of a 2nd. The captives are
        // leashed in a chain : jailer -> captive1 -> captive2 (if any).
        if (!followers.isEmpty() && rng.nextFloat() < 0.30f) {
            VampireEntity jailer = followers.get(rng.nextInt(followers.size()));
            jailer.setJailer(true);

            int captiveCount = rng.nextFloat() < 0.30f ? 2 : 1;
            spawnCaptives(server, jailer, center, captiveCount, rng);
        }

        CollegeOfWinterhold.LOGGER.info(
                "[CovenSpawner] Spawned vampire coven: {} members, leader={}, at {} {} {} (dim={})",
                count, leaderVariantLog, center.getX(), center.getY(), center.getZ(),
                server.dimension().location());

        SpawnDebugBroadcaster.announce(server,
                String.format("Vampire coven — %d members, leader=%s",
                        count,
                        leaderVariantLog == null ? "?" : leaderVariantLog.name().toLowerCase()),
                center);
    }

    /**
     * Spawns captive villagers and leashes them in a chain from the jailer.
     * Each successive captive is leashed to the previous one, not directly
     * to the jailer — producing a visual caravan look.
     */
    private static void spawnCaptives(ServerLevel server, VampireEntity jailer,
                                      BlockPos covenCenter, int count, RandomSource rng) {
        Entity currentHolder = jailer;

        for (int i = 0; i < count; i++) {
            Villager villager = EntityType.VILLAGER.create(server);
            if (villager == null) continue;

            BlockPos jailerPos = jailer.blockPosition();
            double ox = (rng.nextDouble() - 0.5) * 3.0;
            double oz = (rng.nextDouble() - 0.5) * 3.0;
            villager.moveTo(
                    jailerPos.getX() + 0.5 + ox,
                    jailerPos.getY(),
                    jailerPos.getZ() + 0.5 + oz,
                    rng.nextFloat() * 360f, 0f
            );

            villager.getData(ModAttachments.CAPTURED_STATE.get()).markCaptured();
            villager.goalSelector.addGoal(0, new CapturedPanicGoal(villager));
            villager.goalSelector.addGoal(1, new RescuedSafetyGoal(villager));

            villager.finalizeSpawn(server, server.getCurrentDifficultyAt(jailerPos),
                    MobSpawnType.EVENT, null);
            server.addFreshEntityWithPassengers(villager);
            SpawnDebugBroadcaster.glow(villager);

            villager.setLeashedTo(currentHolder, true);
            currentHolder = villager;
        }
    }
}
