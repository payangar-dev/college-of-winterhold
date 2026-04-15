package com.payangar.collegeofwinterhold.entity.ai;

/**
 * Per-spell buff cooldown contract, implemented by any spell-casting entity that
 * uses {@link WizardPreCombatBuffGoal}. Iron's player-side cooldown/recast tracks
 * no-op for mob casters (see the mob casting quirks memory) so we track them
 * ourselves — this interface is the bridge between the goal and whichever entity
 * owns the cooldown state.
 *
 * <p>Values stored are absolute game ticks at which each spell becomes castable
 * again; the implementing entity is free to persist them in NBT.
 */
public interface BuffCooldownHolder {
    /** {@code true} if the given spell's buff cooldown is still running. */
    boolean isBuffOnCooldown(String spellId);

    /** Registers {@code cooldownTicks} ticks of cooldown starting now. */
    void recordBuffCast(String spellId, int cooldownTicks);
}
