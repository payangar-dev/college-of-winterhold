package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Filtered spell pools for College wizards. Acts as a read-only view over Iron's
 * {@link SpellRegistry}, keeping only spells that are
 * <ol>
 *   <li>members of one of the six recognized College schools (Fire, Ice, Lightning,
 *       Holy, Nature, Ender — Blood / Eldritch / Evocation are not taught), and</li>
 *   <li>present in {@link SpellClassificationRegistry} (the implicit College
 *       whitelist — utility spells like {@code touch_dig} or {@code recall} are
 *       intentionally absent and never rolled).</li>
 * </ol>
 *
 * <p>Pools are computed on demand and not cached — wizards roll once per spawn,
 * so the cost is paid at most a handful of times per mob.
 */
public final class CollegeSpellPools {

    public static final List<Supplier<SchoolType>> RECOGNIZED_SCHOOLS = List.of(
            SchoolRegistry.FIRE,
            SchoolRegistry.ICE,
            SchoolRegistry.LIGHTNING,
            SchoolRegistry.HOLY,
            SchoolRegistry.NATURE,
            SchoolRegistry.ENDER
    );

    /**
     * Native pool : every College-classified spell of the given school whose
     * minimum rarity is at most {@code maxRarityInclusive}.
     */
    public static List<AbstractSpell> nativePool(Supplier<SchoolType> school, SpellRarity maxRarityInclusive) {
        int cap = maxRarityInclusive.getValue();
        List<AbstractSpell> result = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.getSpellsForSchool(school.get())) {
            if (!SpellClassificationRegistry.hasEntry(spell)) continue;
            if (spell.getMinRarity() > cap) continue;
            result.add(spell);
        }
        return result;
    }

    /**
     * Foreign pool : the union of every recognized school except {@code nativeSchool},
     * filtered by the same rarity rule. Used by Adept+ wizards to roll their
     * cross-school spells.
     */
    public static List<AbstractSpell> foreignPool(Supplier<SchoolType> nativeSchool, SpellRarity maxRarityInclusive) {
        List<AbstractSpell> result = new ArrayList<>();
        for (Supplier<SchoolType> school : RECOGNIZED_SCHOOLS) {
            if (school == nativeSchool) continue;
            result.addAll(nativePool(school, maxRarityInclusive));
        }
        return result;
    }

    /**
     * Picks {@code count} spells at random from {@code pool} without replacement.
     * Returns fewer than {@code count} spells if the pool is too small.
     */
    public static List<AbstractSpell> pickRandom(List<AbstractSpell> pool, int count, java.util.Random rng) {
        if (pool.isEmpty() || count <= 0) return List.of();
        List<AbstractSpell> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, rng);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    /**
     * Picks {@code count} spells from {@code pool} and rolls a cast level for each
     * in {@code [minLevel, maxLevel]}, clamped to each spell's own {@code getMaxLevel()}.
     * Used by wizards at spawn time to build their loadout.
     */
    public static List<RolledSpell> rollLoadout(List<AbstractSpell> pool, int count, int minLevel, int maxLevel, java.util.Random rng) {
        List<AbstractSpell> picked = pickRandom(pool, count, rng);
        List<RolledSpell> result = new ArrayList<>(picked.size());
        int span = Math.max(1, maxLevel - minLevel + 1);
        for (AbstractSpell spell : picked) {
            int rolled = minLevel + rng.nextInt(span);
            int clamped = Math.max(1, Math.min(rolled, spell.getMaxLevel()));
            result.add(new RolledSpell(spell, clamped));
        }
        return result;
    }

    private CollegeSpellPools() {}
}
