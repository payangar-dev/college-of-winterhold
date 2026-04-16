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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Player-centric radial vampire coven spawner.
 *
 * <p><b>Why not {@code ChunkEvent.Load}</b> — the NeoForge javadoc explicitly
 * warns : <i>"You will cause chunk loading deadlocks if you don't delay your
 * level interactions."</i>
 *
 * <p><b>Why not a grid-cell approach</b> — a previous iteration used a grid of
 * {@code CELL_CHUNKS}-sized cells with deterministic rolls, but the cells were
 * bigger than the player's simulation distance so random anchors in adjacent
 * cells fell outside loaded chunks and {@code hasChunk} rejected almost all
 * tentative spawns. Only one coven per very long walk was materialising.
 *
 * <p><b>Pattern</b> — every {@value #SCAN_INTERVAL} ticks, each player triggers
 * {@value #ATTEMPTS_PER_SCAN} attempts. Each attempt picks a random point in a
 * ring around the player (between {@value #MIN_SPAWN_DIST} and
 * {@value #MAX_SPAWN_DIST} blocks — inside simulation distance, outside the
 * player's face), gates against {@link #SPAWN_CHANCE_PER_ATTEMPT}, checks
 * biome/ground/spacing and spawns the coven if everything clears. Spacing is
 * enforced by rejecting any candidate too close to an existing vampire — no
 * grid, no persistence, no PROCESSED set.
 *
 * <p><b>Refreshable covens</b> — because spacing is the only density gate, a
 * cleared area naturally repopulates once the previous members despawn (via
 * {@code removeWhenFarAway → true}) : the spacing check passes again on the
 * next scan and a fresh coven is materialised.
 *
 * <p><b>Dark-forest grounding</b> — no vanilla heightmap ignores logs, so
 * {@code MOTION_BLOCKING_NO_LEAVES} returns the top of a trunk under the
 * canopy. {@link #findDarkForestGround} walks down from {@code WORLD_SURFACE}
 * skipping leaves, logs and replaceables. A single attempt that lands on a
 * trunk returns {@code null} ; callers retry with a fresh random position.
 */
@EventBusSubscriber(modid = CollegeOfWinterhold.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class VampireCovenSpawner {

    private VampireCovenSpawner() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        int interval = ModServerConfig.COVEN_SCAN_INTERVAL_TICKS.get();
        if (server.getServer().getTickCount() % interval != 0) return;

        // Covens only materialise at night — vampires burn in daylight and
        // a coven spawning during the day would be immediately cooked. Also
        // fits the lore of roaming blood mages lurking after dusk.
        if (!server.isNight()) return;

        for (Player player : server.players()) {
            tryNearPlayer(server, player);
        }
    }

    /**
     * Runs up to {@code attemptsPerScan} spawn attempts (from config) in a
     * ring around the given player. Each attempt is independent : earlier
     * failures don't consume the remaining attempts.
     */
    private static void tryNearPlayer(ServerLevel server, Player player) {
        RandomSource rng = server.getRandom();

        int attempts = ModServerConfig.COVEN_ATTEMPTS_PER_SCAN.get();
        double spawnChance = ModServerConfig.COVEN_SPAWN_CHANCE.get();
        int minDist = ModServerConfig.COVEN_MIN_SPAWN_DISTANCE.get();
        int maxDist = ModServerConfig.COVEN_MAX_SPAWN_DISTANCE.get();
        int spacing = ModServerConfig.COVEN_MIN_SPACING_BLOCKS.get();

        for (int i = 0; i < attempts; i++) {
            if (rng.nextDouble() > spawnChance) continue;

            double angle = rng.nextDouble() * Math.PI * 2.0;
            double distance = minDist + rng.nextDouble() * Math.max(0, maxDist - minDist);
            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);

            int chunkX = x >> 4;
            int chunkZ = z >> 4;

            // Only touch chunks that are already fully loaded — the whole
            // point of this pattern is to never load a chunk synchronously.
            if (!server.hasChunk(chunkX, chunkZ)) continue;
            if (server.getChunkSource().getChunkNow(chunkX, chunkZ) == null) continue;

            BlockPos probe = new BlockPos(x, 64, z);
            if (!server.getBiome(probe).is(VampireBiomeTags.VAMPIRE_COVEN_BIOMES)) continue;

            BlockPos ground = findDarkForestGround(server, x, z);
            if (ground == null) continue;

            // Spacing gate : reject if any vampire already exists within
            // the configured spacing radius of the candidate ground spot.
            AABB spacingBox = new AABB(ground).inflate(spacing);
            if (!server.getEntitiesOfClass(VampireEntity.class, spacingBox).isEmpty()) continue;

            spawnCoven(server, ground, rng);
        }
    }

    /**
     * Single-column walk-down : starts at {@code WORLD_SURFACE}, skips air,
     * leaves, logs and replaceable plants, and stops at the first block whose
     * top face is sturdy and which has two blocks of empty collision above.
     * Returns {@code null} when the column is blocked (e.g. under a trunk).
     */
    private static BlockPos findDarkForestGround(ServerLevel server, int x, int z) {
        int topY = server.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int minY = server.getMinBuildHeight() + 2;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, topY, z);

        for (int y = topY; y >= minY; y--) {
            cursor.setY(y);
            BlockState state = server.getBlockState(cursor);

            if (state.isAir()
                    || state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.LOGS)
                    || state.is(BlockTags.REPLACEABLE)) {
                continue;
            }

            if (!state.isFaceSturdy(server, cursor, Direction.UP)) return null;

            BlockPos foot = new BlockPos(x, y + 1, z);
            if (!server.getBlockState(foot).getCollisionShape(server, foot).isEmpty()) return null;
            BlockPos head = foot.above();
            if (!server.getBlockState(head).getCollisionShape(server, head).isEmpty()) return null;
            return foot;
        }
        return null;
    }

    /**
     * Spawns a coven at the given ground position. Exposed for the debug
     * command ({@code /cow spawn_coven}) and for the radial scan above.
     * Expects {@code center} to already be a valid walkable ground spot —
     * the caller is responsible for running it through {@code findDarkForestGround}
     * if it came from an arbitrary source.
     */
    public static void spawnCoven(ServerLevel server, BlockPos center, RandomSource rng) {
        int count = 3 + rng.nextInt(3); // 3, 4 or 5
        VampireEntity leader = null;
        VampireVariant leaderVariantLog = null;
        var followers = new ArrayList<VampireEntity>();

        for (int i = 0; i < count; i++) {
            int dx = rng.nextInt(7) - 3;
            int dz = rng.nextInt(7) - 3;
            BlockPos foot = findDarkForestGround(server, center.getX() + dx, center.getZ() + dz);
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
        }

        // Jailer + captives : 30% of covens have a jailer. When present,
        // always 1 captive villager, 30% chance of a 2nd. The captives are
        // leashed in a chain : jailer → captive1 → captive2 (if any).
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

            villager.setLeashedTo(currentHolder, true);
            currentHolder = villager;
        }
    }
}
