package com.payangar.collegeofwinterhold.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side config for College of Winterhold. Values are persisted in
 * {@code serverconfig/college_of_winterhold-server.toml} and reloaded on
 * world load, so tuning a value only requires restarting the world (not
 * rebuilding the mod).
 */
public final class ModServerConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ────────────────────────────────────────────────────────────────────────
    // [vampire.coven]

    public static final ModConfigSpec.IntValue COVEN_SCAN_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue COVEN_ATTEMPTS_PER_SCAN;
    public static final ModConfigSpec.DoubleValue COVEN_SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue COVEN_MIN_SPACING_BLOCKS;
    public static final ModConfigSpec.IntValue COVEN_MIN_SPAWN_DISTANCE;
    public static final ModConfigSpec.IntValue COVEN_MAX_SPAWN_DISTANCE;
    public static final ModConfigSpec.DoubleValue COVEN_FORWARD_BIAS;

    // ────────────────────────────────────────────────────────────────────────
    // [wizard.exploration]

    public static final ModConfigSpec.IntValue EXPLORATION_SCAN_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue EXPLORATION_ATTEMPTS_PER_SCAN;
    public static final ModConfigSpec.DoubleValue EXPLORATION_BASE_SPAWN_CHANCE;
    public static final ModConfigSpec.DoubleValue EXPLORATION_STRUCTURE_MULTIPLIER;
    public static final ModConfigSpec.IntValue EXPLORATION_MIN_SPACING_BLOCKS;
    public static final ModConfigSpec.IntValue EXPLORATION_MIN_SPAWN_DISTANCE;
    public static final ModConfigSpec.IntValue EXPLORATION_MAX_SPAWN_DISTANCE;
    public static final ModConfigSpec.DoubleValue EXPLORATION_FORWARD_BIAS;

    // ────────────────────────────────────────────────────────────────────────
    // [debug]

    public static final ModConfigSpec.BooleanValue DEBUG_SPAWN_ANNOUNCEMENTS;

    public static final ModConfigSpec SPEC;

    static {
        // ── Vampire ─────────────────────────────────────────────────────────
        BUILDER.push("vampire");
        BUILDER.push("coven");

        COVEN_SCAN_INTERVAL_TICKS = BUILDER
                .comment(
                        "How often (in ticks) the coven spawner runs a scan pass around every player.",
                        "20 ticks = 1 second. Lower = more frequent spawn opportunities, higher CPU cost.",
                        "Default: 600 (30 seconds)."
                )
                .defineInRange("scanIntervalTicks", 600, 20, 72000);

        COVEN_ATTEMPTS_PER_SCAN = BUILDER
                .comment(
                        "Fallback attempts per scan per player. Each attempt picks a random point",
                        "in a directional cone around the player and runs biome / ground / chance /",
                        "spacing checks. The FIRST attempt that passes every check spawns the coven",
                        "and the remaining attempts are skipped — attempts are fallbacks for",
                        "rejected positions, not multipliers. More attempts = higher odds any single",
                        "scan produces at least one spawn.",
                        "Default: 5."
                )
                .defineInRange("attemptsPerScan", 5, 1, 50);

        COVEN_SPAWN_CHANCE = BUILDER
                .comment(
                        "Probability (0.0 - 1.0) that an individual spawn attempt proceeds to the",
                        "biome / ground / spacing checks. Set to 1.0 for aggressive testing, ~0.15",
                        "for production-like rarity.",
                        "Default: 1.0."
                )
                .defineInRange("spawnChance", 1.0, 0.0, 1.0);

        COVEN_MIN_SPACING_BLOCKS = BUILDER
                .comment(
                        "Minimum distance (blocks) between two coven centres. Hard cap on density :",
                        "no matter how high spawnChance is, covens won't cluster closer than this.",
                        "A despawned coven frees the area for a fresh spawn once the vampires are gone.",
                        "Default: 96."
                )
                .defineInRange("minSpacingBlocks", 96, 16, 1000);

        COVEN_MIN_SPAWN_DISTANCE = BUILDER
                .comment(
                        "Minimum distance (blocks) from the triggering player before a coven can pop in.",
                        "Avoids covens materialising directly in the player's face.",
                        "Default: 48."
                )
                .defineInRange("minSpawnDistance", 48, 0, 512);

        COVEN_MAX_SPAWN_DISTANCE = BUILDER
                .comment(
                        "Maximum distance (blocks) from the triggering player where a coven may spawn.",
                        "Must stay within simulation distance (~160 blocks for a vanilla client) to",
                        "guarantee the anchor chunk is loaded.",
                        "Default: 128."
                )
                .defineInRange("maxSpawnDistance", 128, 16, 512);

        COVEN_FORWARD_BIAS = BUILDER
                .comment(
                        "Bias spawn positions toward the player's movement/look direction.",
                        "0.0 = uniform random angle (full ring, legacy behavior).",
                        "1.0 = tight cone in front of the player.",
                        "Default: 0.6."
                )
                .defineInRange("forwardBias", 0.6, 0.0, 1.0);

        BUILDER.pop(); // coven
        BUILDER.pop(); // vampire

        // ── Wizard ──────────────────────────────────────────────────────────
        BUILDER.push("wizard");
        BUILDER.push("exploration");

        EXPLORATION_SCAN_INTERVAL_TICKS = BUILDER
                .comment(
                        "How often (in ticks) the exploration spawner runs a scan pass around every player.",
                        "20 ticks = 1 second. Higher values = rarer spawns.",
                        "Default: 1200 (60 seconds)."
                )
                .defineInRange("scanIntervalTicks", 1200, 20, 72000);

        EXPLORATION_ATTEMPTS_PER_SCAN = BUILDER
                .comment(
                        "Fallback attempts per scan per player. The first attempt that passes every",
                        "check spawns the group and the rest are skipped — attempts are fallbacks",
                        "for rejected positions (wrong biome, bad ground, spacing conflict), not",
                        "multipliers.",
                        "Default: 3."
                )
                .defineInRange("attemptsPerScan", 3, 1, 50);

        EXPLORATION_BASE_SPAWN_CHANCE = BUILDER
                .comment(
                        "Base probability (0.0 - 1.0) that an individual spawn attempt proceeds.",
                        "This is the chance in open terrain — structures multiply this value by",
                        "the structureMultiplier below (capped at 1.0).",
                        "Default: 0.15."
                )
                .defineInRange("baseSpawnChance", 0.15, 0.0, 1.0);

        EXPLORATION_STRUCTURE_MULTIPLIER = BUILDER
                .comment(
                        "Multiplier applied to baseSpawnChance when the candidate position is",
                        "inside a structure (village, temple, stronghold, etc.).",
                        "Effective chance = min(baseSpawnChance * structureMultiplier, 1.0).",
                        "Default: 3.0."
                )
                .defineInRange("structureMultiplier", 3.0, 1.0, 20.0);

        EXPLORATION_MIN_SPACING_BLOCKS = BUILDER
                .comment(
                        "Minimum distance (blocks) between two exploration wizard groups.",
                        "Default: 128."
                )
                .defineInRange("minSpacingBlocks", 128, 16, 1000);

        EXPLORATION_MIN_SPAWN_DISTANCE = BUILDER
                .comment(
                        "Minimum distance (blocks) from the triggering player.",
                        "Default: 48."
                )
                .defineInRange("minSpawnDistance", 48, 0, 512);

        EXPLORATION_MAX_SPAWN_DISTANCE = BUILDER
                .comment(
                        "Maximum distance (blocks) from the triggering player.",
                        "Default: 128."
                )
                .defineInRange("maxSpawnDistance", 128, 16, 512);

        EXPLORATION_FORWARD_BIAS = BUILDER
                .comment(
                        "Bias spawn positions toward the player's movement/look direction.",
                        "0.0 = uniform random angle (full ring, legacy behavior).",
                        "1.0 = tight cone in front of the player.",
                        "Default: 0.6."
                )
                .defineInRange("forwardBias", 0.6, 0.0, 1.0);

        BUILDER.pop(); // exploration
        BUILDER.pop(); // wizard

        // ── Debug ───────────────────────────────────────────────────────────
        BUILDER.push("debug");

        DEBUG_SPAWN_ANNOUNCEMENTS = BUILDER
                .comment(
                        "When true, every coven / exploration group spawn broadcasts a",
                        "clickable chat message (same pattern as vanilla /locate) to all",
                        "players and gives each spawned member a 30 s Glowing effect.",
                        "Test aid — leave false in normal play.",
                        "Default: false."
                )
                .define("spawnAnnouncements", false);

        BUILDER.pop(); // debug

        SPEC = BUILDER.build();
    }

    private ModServerConfig() {}
}
