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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

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
 * <p>Groups are composed of a single Adept+ leader optionally accompanied
 * by 1–2 Novice/Apprentice followers of the same school. If the leader
 * dies, followers become independent (their {@link com.payangar.collegeofwinterhold.entity.ai.FollowLeaderGoal}
 * yields when the leader UUID resolves to null).
 *
 * <p>Exploration wizards despawn when far from players, unlike village
 * wizards which are persistent.
 *
 * <p>Pattern mirrors {@link VampireCovenSpawner}: {@code LevelTickEvent.Post}
 * radial scan around each player, spacing-gated, configurable via
 * {@link ModServerConfig}.
 */
@EventBusSubscriber(modid = CollegeOfWinterhold.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class WizardExplorationSpawner {

    private WizardExplorationSpawner() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        int interval = ModServerConfig.EXPLORATION_SCAN_INTERVAL_TICKS.get();
        if (server.getServer().getTickCount() % interval != 0) return;

        for (Player player : server.players()) {
            tryNearPlayer(server, player);
        }
    }

    private static void tryNearPlayer(ServerLevel server, Player player) {
        RandomSource rng = server.getRandom();

        int attempts = ModServerConfig.EXPLORATION_ATTEMPTS_PER_SCAN.get();
        double baseChance = ModServerConfig.EXPLORATION_BASE_SPAWN_CHANCE.get();
        double structureMult = ModServerConfig.EXPLORATION_STRUCTURE_MULTIPLIER.get();
        int minDist = ModServerConfig.EXPLORATION_MIN_SPAWN_DISTANCE.get();
        int maxDist = ModServerConfig.EXPLORATION_MAX_SPAWN_DISTANCE.get();
        int spacing = ModServerConfig.EXPLORATION_MIN_SPACING_BLOCKS.get();

        for (int i = 0; i < attempts; i++) {
            double angle = rng.nextDouble() * Math.PI * 2.0;
            double distance = minDist + rng.nextDouble() * Math.max(0, maxDist - minDist);
            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            if (!server.hasChunk(chunkX, chunkZ)) continue;
            if (server.getChunkSource().getChunkNow(chunkX, chunkZ) == null) continue;

            // Surface position — wizards are explorers travelling overworld
            int y = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos ground = new BlockPos(x, y, z);

            // Structure boost: multiply base chance if inside any structure
            double effectiveChance = baseChance;
            Map<Structure, ?> structures = server.structureManager()
                    .getAllStructuresAt(ground);
            if (!structures.isEmpty()) {
                effectiveChance = Math.min(baseChance * structureMult, 1.0);
            }

            if (rng.nextDouble() > effectiveChance) continue;

            // Spacing gate: reject if any college wizard already exists nearby
            AABB spacingBox = new AABB(ground).inflate(spacing);
            if (!server.getEntitiesOfClass(AbstractCollegeWizardEntity.class, spacingBox,
                    AbstractCollegeWizardEntity::isExplorationGroup).isEmpty()) {
                continue;
            }

            spawnExplorationGroup(server, ground, rng);
        }
    }

    /**
     * Spawns an exploration group at the given position. Exposed for the debug
     * command ({@code /cow spawn_exploration}) and for the radial scan above.
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

        if (followerCount > 0) {
            leader.setGroupLeader(true);
        }

        leader.finalizeSpawn(server, server.getCurrentDifficultyAt(center),
                MobSpawnType.EVENT, null);
        server.addFreshEntityWithPassengers(leader);

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
            int fy = server.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    center.getX() + (int) ox, center.getZ() + (int) oz);
            follower.moveTo(center.getX() + 0.5 + ox, fy,
                    center.getZ() + 0.5 + oz, rng.nextFloat() * 360f - 180f, 0f);

            follower.setLeaderUuid(leader.getUUID());

            follower.finalizeSpawn(server, server.getCurrentDifficultyAt(center),
                    MobSpawnType.EVENT, null);
            server.addFreshEntityWithPassengers(follower);
        }

        CollegeOfWinterhold.LOGGER.info(
                "[ExplorationSpawner] Spawned {} {} wizard(s): leader={}, followers={}, at {} {} {} (dim={})",
                1 + followerCount, school.name().toLowerCase(), leaderTier.name().toLowerCase(),
                followerCount, center.getX(), center.getY(), center.getZ(),
                server.dimension().location());
    }
}
