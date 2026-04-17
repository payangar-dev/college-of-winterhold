package com.payangar.collegeofwinterhold.world;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.config.ModServerConfig;
import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import com.payangar.collegeofwinterhold.entity.wizard.core.CollegeSchool;
import com.payangar.collegeofwinterhold.entity.wizard.core.WizardTier;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;

import java.util.Map;

/**
 * Player-centric radial spawner for wizard exploration parties.
 *
 * <p>Wizards from any school may appear at the surface in any biome —
 * they are scholars on an expedition, not bound to their home territory.
 * Spawn chance is very low in the open but multiplied inside structures
 * (villages, temples, strongholds, etc.) where a travelling mage would
 * logically stop to study.
 *
 * <p>See {@link AbstractGroupSpawner} for the shared spawning algorithm
 * (directional cone, adaptive attempts, chunk safety, spacing gate).
 */
public class WizardExplorationSpawner extends AbstractGroupSpawner {

    public static final WizardExplorationSpawner INSTANCE = new WizardExplorationSpawner();

    WizardExplorationSpawner() {}

    // ── Config hooks ────────────────────────────────────────────────────────

    @Override protected int scanIntervalTicks() { return ModServerConfig.EXPLORATION_SCAN_INTERVAL_TICKS.get(); }
    @Override protected int attemptsPerScan()    { return ModServerConfig.EXPLORATION_ATTEMPTS_PER_SCAN.get(); }
    @Override protected int minSpawnDistance()    { return ModServerConfig.EXPLORATION_MIN_SPAWN_DISTANCE.get(); }
    @Override protected int maxSpawnDistance()    { return ModServerConfig.EXPLORATION_MAX_SPAWN_DISTANCE.get(); }
    @Override protected int minSpacingBlocks()    { return ModServerConfig.EXPLORATION_MIN_SPACING_BLOCKS.get(); }
    @Override protected double forwardBias()      { return ModServerConfig.EXPLORATION_FORWARD_BIAS.get(); }

    // ── Behavioral hooks ────────────────────────────────────────────────────

    @Override
    protected double spawnChance(ServerLevel server, BlockPos ground) {
        double base = ModServerConfig.EXPLORATION_BASE_SPAWN_CHANCE.get();
        double mult = ModServerConfig.EXPLORATION_STRUCTURE_MULTIPLIER.get();
        Map<Structure, ?> structures = server.structureManager().getAllStructuresAt(ground);
        if (!structures.isEmpty()) {
            return Math.min(base * mult, 1.0);
        }
        return base;
    }

    @Override
    protected boolean isSpacingClear(ServerLevel server, BlockPos ground) {
        AABB box = new AABB(ground).inflate(minSpacingBlocks());
        return server.getEntitiesOfClass(AbstractCollegeWizardEntity.class, box,
                AbstractCollegeWizardEntity::isExplorationGroup).isEmpty();
    }

    @Override
    protected void spawnGroup(ServerLevel server, BlockPos pos, RandomSource rng) {
        spawnExplorationGroup(server, pos, rng);
    }

    // ── Group composition (public static for debug command) ─────────────────

    /**
     * Spawns an exploration group at the given position. Exposed for the debug
     * command ({@code /cow spawn_exploration}) and for the spawner above.
     */
    public static void spawnExplorationGroup(ServerLevel server, BlockPos center, RandomSource rng) {
        // Random school — exploration is not biome-bound
        CollegeSchool[] schools = CollegeSchool.values();
        CollegeSchool school = schools[rng.nextInt(schools.length)];

        // Leader: Adept+ (70/25/5 distribution)
        WizardTier leaderTier = WizardTier.rollAdeptPlus(rng);

        // Follower count: 50% solo, 30% +1, 20% +2
        int followerCount;
        float roll = rng.nextFloat();
        if (roll < 0.50f) {
            followerCount = 0;
        } else if (roll < 0.80f) {
            followerCount = 1;
        } else {
            followerCount = 2;
        }

        // Spawn leader
        EntityType<? extends AbstractCollegeWizardEntity> leaderType = ModEntityTypes.wizardFor(school, leaderTier);
        if (leaderType == null) return;

        AbstractCollegeWizardEntity leader = leaderType.create(server);
        if (leader == null) return;

        leader.moveTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5,
                rng.nextFloat() * 360f - 180f, 0f);

        // Always flag the leader, even when solo — a solo wizard is still the
        // (trivial) leader of its own exploration party. This is what makes
        // isExplorationGroup() → true, so the spacing gate sees solo wizards
        // and removeWhenFarAway() despawns them like grouped ones.
        leader.setGroupLeader(true);

        leader.finalizeSpawn(server, server.getCurrentDifficultyAt(center),
                MobSpawnType.EVENT, null);
        server.addFreshEntityWithPassengers(leader);
        SpawnDebugBroadcaster.glow(leader);

        // Spawn followers — same school as leader
        for (int i = 0; i < followerCount; i++) {
            WizardTier followerTier = rng.nextFloat() < 0.50f
                    ? WizardTier.NOVICE
                    : WizardTier.APPRENTICE;

            EntityType<? extends AbstractCollegeWizardEntity> followerType =
                    ModEntityTypes.wizardFor(school, followerTier);
            if (followerType == null) continue;

            AbstractCollegeWizardEntity follower = followerType.create(server);
            if (follower == null) continue;

            double ox = (rng.nextDouble() - 0.5) * 5.0;
            double oz = (rng.nextDouble() - 0.5) * 5.0;
            int fx = center.getX() + (int) ox;
            int fz = center.getZ() + (int) oz;
            BlockPos fFoot = AbstractGroupSpawner.walkDownToSurface(server, fx, fz);
            if (fFoot == null) fFoot = center;
            follower.moveTo(fFoot.getX() + 0.5, fFoot.getY(),
                    fFoot.getZ() + 0.5, rng.nextFloat() * 360f - 180f, 0f);

            follower.setLeaderUuid(leader.getUUID());

            follower.finalizeSpawn(server, server.getCurrentDifficultyAt(center),
                    MobSpawnType.EVENT, null);
            server.addFreshEntityWithPassengers(follower);
            SpawnDebugBroadcaster.glow(follower);
        }

        CollegeOfWinterhold.LOGGER.info(
                "[ExplorationSpawner] Spawned {} {} wizard(s): leader={}, followers={}, at {} {} {} (dim={})",
                1 + followerCount, school.name().toLowerCase(), leaderTier.name().toLowerCase(),
                followerCount, center.getX(), center.getY(), center.getZ(),
                server.dimension().location());

        SpawnDebugBroadcaster.announce(server,
                String.format("Exploration party — %s %s (%d follower%s)",
                        school.name().toLowerCase(),
                        leaderTier.name().toLowerCase(),
                        followerCount,
                        followerCount == 1 ? "" : "s"),
                center);
    }
}
