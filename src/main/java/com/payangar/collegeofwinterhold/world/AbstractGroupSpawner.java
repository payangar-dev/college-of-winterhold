package com.payangar.collegeofwinterhold.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Template-method base class for player-centric radial group spawners.
 *
 * <p>Encapsulates the shared algorithm: interval gating, player iteration,
 * adaptive cone-biased position selection, chunk safety, biome/ground/spacing
 * validation. Subclasses provide config values and behavioral hooks.
 *
 * <p>Instances are singletons held by {@link GroupSpawnManager}, which calls
 * {@link #tick(ServerLevel)} every server tick via a single
 * {@code @EventBusSubscriber}.
 *
 * @see VampireCovenSpawner
 * @see WizardExplorationSpawner
 */
public abstract class AbstractGroupSpawner {

    // ── Config hooks ────────────────────────────────────────────────────────

    /** Ticks between scan passes. */
    protected abstract int scanIntervalTicks();

    /** Independent spawn attempts per player per scan. */
    protected abstract int attemptsPerScan();

    /** Minimum distance (blocks) from the triggering player. */
    protected abstract int minSpawnDistance();

    /** Maximum distance (blocks) from the triggering player. */
    protected abstract int maxSpawnDistance();

    /** Minimum distance between two group centres (spacing gate). */
    protected abstract int minSpacingBlocks();

    /** Directional bias: 0.0 = uniform ring, 1.0 = tight forward cone. */
    protected abstract double forwardBias();

    // ── Behavioral hooks ────────────────────────────────────────────────────

    /**
     * Time-of-day or dimension gate. Called once per scan cycle.
     * Default: always allowed.
     */
    protected boolean canSpawnNow(ServerLevel server) {
        return true;
    }

    /**
     * Effective spawn chance at the given resolved ground position.
     * Called after ground resolution so subclasses can inspect the
     * position (e.g. structure boost).
     */
    protected abstract double spawnChance(ServerLevel server, BlockPos ground);

    /**
     * Biome filter at the probe position (Y=64 for biome lookup).
     * Default: all biomes accepted.
     */
    protected boolean isValidBiome(ServerLevel server, BlockPos probe) {
        return true;
    }

    /**
     * Resolve a walkable ground position from world coordinates.
     * Default: delegates to {@link #walkDownToSurface(ServerLevel, int, int)}.
     *
     * @return a valid foot-level position, or {@code null} to skip this attempt
     */
    protected BlockPos findGround(ServerLevel server, int x, int z) {
        return walkDownToSurface(server, x, z);
    }

    /**
     * Single-column walk-down starting at {@code WORLD_SURFACE}. Skips air,
     * leaves, logs and replaceable plants, and stops at the first block whose
     * top face is sturdy and which has two blocks of empty collision above.
     * Returns {@code null} when the column is blocked (e.g. under a trunk)
     * or when the first solid surface is a fluid (ocean, lava lake).
     *
     * <p>This is strictly safer than {@code MOTION_BLOCKING_NO_LEAVES}:
     * <ul>
     *   <li>Logs are not treated as ground — a random XZ point landing on a
     *       tree trunk no longer spawns the mob in the canopy.</li>
     *   <li>Water / lava fail {@code isFaceSturdy} → no spawns on oceans.</li>
     *   <li>Tall grass / flowers are walked through to the soil below.</li>
     * </ul>
     */
    public static BlockPos walkDownToSurface(ServerLevel server, int x, int z) {
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
     * Returns {@code true} if the spacing gate passes — no existing group
     * is too close to the candidate position.
     */
    protected abstract boolean isSpacingClear(ServerLevel server, BlockPos ground);

    /** Spawn the actual group at the finalized position. */
    protected abstract void spawnGroup(ServerLevel server, BlockPos pos, RandomSource rng);

    // ── Template method ─────────────────────────────────────────────────────

    /**
     * Called every server tick by {@link GroupSpawnManager}. Gates on scan
     * interval and time-of-day, then iterates over all players.
     */
    public final void tick(ServerLevel server) {
        if (server.getServer().getTickCount() % scanIntervalTicks() != 0) return;
        if (!canSpawnNow(server)) return;

        for (Player player : server.players()) {
            tryNearPlayer(server, player);
        }
    }

    /**
     * Runs adaptive spawn attempts in a directional cone around the player.
     *
     * <p>First attempts target far positions in a tight forward cone; later
     * attempts progressively widen the angle and reduce the distance, acting
     * as graceful fallbacks when ideal positions fail validation.
     */
    private void tryNearPlayer(ServerLevel server, Player player) {
        RandomSource rng = server.getRandom();
        int attempts = attemptsPerScan();
        int minDist = minSpawnDistance();
        int maxDist = maxSpawnDistance();
        double bias = forwardBias();

        for (int i = 0; i < attempts; i++) {
            // ── Adaptive distance: far → close ──────────────────────────
            double distance = SpawnPositionUtils.adaptDistance(minDist, maxDist, i, attempts);
            // Small jitter so attempts at the same index aren't identical
            distance += rng.nextDouble() * 8.0 - 4.0;
            distance = Math.max(minDist, Math.min(maxDist, distance));

            // ── Adaptive cone: tight → wide ─────────────────────────────
            double effectiveBias = SpawnPositionUtils.adaptBias(bias, i, attempts);
            double angle = SpawnPositionUtils.pickConeAngle(player, rng, effectiveBias);

            int x = (int) (player.getX() + Math.cos(angle) * distance);
            int z = (int) (player.getZ() + Math.sin(angle) * distance);

            // ── Chunk safety ────────────────────────────────────────────
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            if (!server.hasChunk(chunkX, chunkZ)) continue;
            if (server.getChunkSource().getChunkNow(chunkX, chunkZ) == null) continue;

            // ── Biome gate ──────────────────────────────────────────────
            if (!isValidBiome(server, new BlockPos(x, 64, z))) continue;

            // ── Ground resolution ───────────────────────────────────────
            BlockPos ground = findGround(server, x, z);
            if (ground == null) continue;

            // ── Spawn chance (after ground so structure-based boosts work)
            if (rng.nextDouble() > spawnChance(server, ground)) continue;

            // ── Spacing gate ────────────────────────────────────────────
            if (!isSpacingClear(server, ground)) continue;

            // ── Spawn ───────────────────────────────────────────────────
            // One successful spawn per scan per player. Remaining attempts
            // are fallbacks for position rejection, not multipliers.
            spawnGroup(server, ground, rng);
            return;
        }
    }
}
