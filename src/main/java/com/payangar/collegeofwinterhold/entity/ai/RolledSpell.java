package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;

/**
 * A spell rolled into a wizard's loadout at spawn time, paired with its cast level.
 * Each wizard's known spells are stored as a list of these so the goal can use
 * the per-spell level instead of Iron's global {@code setSpellQuality} range.
 */
public record RolledSpell(AbstractSpell spell, int level) {
}
