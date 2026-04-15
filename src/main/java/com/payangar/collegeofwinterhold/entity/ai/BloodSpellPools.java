package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;

import java.util.ArrayList;
import java.util.List;

/**
 * Blood-school spell pool for hostile casters (Vampires). Mirrors
 * {@link CollegeSpellPools} but targets the Blood school exclusively — the
 * College explicitly refuses blood magic, so its own pools skip it entirely.
 *
 * <p>Filters are the same two rules used by the College: only spells classified
 * in {@link SpellClassificationRegistry} and only those whose minimum rarity is
 * at most the cap. Pools are recomputed on demand — each vampire rolls once at
 * spawn so the cost is negligible.
 */
public final class BloodSpellPools {

    /**
     * Every Blood-classified spell whose minimum rarity is at most
     * {@code maxRarityInclusive}. Utility spells absent from
     * {@link SpellClassificationRegistry} (e.g. Sacrifice, which needs
     * friendly-target data the AI cannot supply) are skipped.
     */
    public static List<AbstractSpell> pool(SpellRarity maxRarityInclusive) {
        int cap = maxRarityInclusive.getValue();
        List<AbstractSpell> result = new ArrayList<>();
        for (AbstractSpell spell : SpellRegistry.getSpellsForSchool(SchoolRegistry.BLOOD.get())) {
            if (!SpellClassificationRegistry.hasEntry(spell)) continue;
            if (spell.getMinRarity() > cap) continue;
            result.add(spell);
        }
        return result;
    }

    private BloodSpellPools() {}
}
