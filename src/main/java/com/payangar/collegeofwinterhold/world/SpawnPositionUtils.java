package com.payangar.collegeofwinterhold.world;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Pure-math utilities for directional cone spawning and adaptive attempts.
 *
 * <p>All methods are stateless and side-effect-free — they only compute
 * angles and distances from player state and RNG.
 */
public final class SpawnPositionUtils {

    private static final double TWO_PI = Math.PI * 2.0;

    /** Horizontal speed² below which the player is considered stationary. */
    private static final double MOVE_THRESHOLD_SQ = 1.0E-4;

    /** XZ magnitude² below which a direction vector is degenerate. */
    private static final double DIR_EPSILON_SQ = 1.0E-8;

    private SpawnPositionUtils() {}

    /**
     * Returns the XZ-plane angle (radians) representing the player's forward
     * direction. Prefers the movement vector when the player is walking/flying;
     * falls back to the look direction when stationary.
     *
     * <p>If both vectors are near-zero in XZ (e.g. looking straight down while
     * stationary), returns {@code 0.0} as a neutral fallback — {@code forwardBias}
     * will spread spawns around it.
     */
    public static double playerForwardAngle(Player player) {
        Vec3 delta = player.getDeltaMovement();
        double hDistSq = delta.x * delta.x + delta.z * delta.z;

        double dx, dz;
        if (hDistSq > MOVE_THRESHOLD_SQ) {
            dx = delta.x;
            dz = delta.z;
        } else {
            Vec3 look = player.getLookAngle();
            dx = look.x;
            dz = look.z;
        }

        if (dx * dx + dz * dz < DIR_EPSILON_SQ) {
            return 0.0;
        }

        return Math.atan2(dz, dx);
    }

    /**
     * Picks a spawn angle biased toward the player's forward direction.
     *
     * <p>When {@code effectiveBias <= 0}, returns a uniform random angle in
     * {@code [0, 2π)} — equivalent to the legacy full-ring behavior.
     *
     * <p>When {@code effectiveBias > 0}, returns a Gaussian-distributed angle
     * centered on the player's forward direction. The standard deviation
     * (sigma) decreases as bias increases:
     * <ul>
     *   <li>{@code bias = 1.0} → sigma ≈ π/3 (≈60°) — tight cone</li>
     *   <li>{@code bias = 0.5} → sigma ≈ π/2 (≈90°) — wide preference</li>
     * </ul>
     *
     * @param effectiveBias 0.0 = uniform, 1.0 = tight cone
     * @return angle in radians, normalized to {@code [0, 2π)}
     */
    public static double pickConeAngle(Player player, RandomSource rng, double effectiveBias) {
        if (effectiveBias <= 0.0) {
            return rng.nextDouble() * TWO_PI;
        }

        double baseAngle = playerForwardAngle(player);

        // sigma = π * (1 - bias) / (1 + 2*bias)
        // Maps bias [0..1] → sigma [π .. π/3] smoothly.
        double sigma = Math.PI * (1.0 - effectiveBias) / (1.0 + 2.0 * effectiveBias);

        double angle = baseAngle + rng.nextGaussian() * sigma;

        // Normalize to [0, 2π)
        angle = angle % TWO_PI;
        if (angle < 0) angle += TWO_PI;
        return angle;
    }

    /**
     * Linearly interpolates spawn distance from far (first attempt) to close
     * (last attempt). Single-attempt spawners receive the midpoint.
     *
     * @param attemptIndex zero-based index of the current attempt
     * @param totalAttempts total number of attempts this scan cycle
     * @return target distance in blocks (not jittered — caller adds noise)
     */
    public static double adaptDistance(int minDist, int maxDist, int attemptIndex, int totalAttempts) {
        if (totalAttempts <= 1) {
            return (minDist + maxDist) / 2.0;
        }
        double t = (double) attemptIndex / (totalAttempts - 1);
        // far → close
        return maxDist - t * (maxDist - minDist);
    }

    /**
     * Scales forward bias from full (first attempt) to 30% (last attempt),
     * progressively widening the cone on fallback attempts. If
     * {@code forwardBias} is 0, always returns 0 (uniform distribution,
     * no adaptation).
     *
     * <p>The last attempt retains 30% of the configured bias rather than
     * going fully uniform, preventing spawns from clustering behind the
     * player on the final fallback.
     *
     * @param attemptIndex zero-based index of the current attempt
     * @param totalAttempts total number of attempts this scan cycle
     */
    public static double adaptBias(double forwardBias, int attemptIndex, int totalAttempts) {
        if (forwardBias <= 0.0) return 0.0;
        if (totalAttempts <= 1) return forwardBias;
        double t = (double) attemptIndex / (totalAttempts - 1);
        return forwardBias * (1.0 - 0.7 * t);
    }
}
