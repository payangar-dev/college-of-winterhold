package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;

import java.util.HashMap;
import java.util.Map;

public final class SpellRangeRegistry {
    private static final Map<String, SpellRange> SPELL_RANGES = new HashMap<>();

    static {
        // Lightning school — validated classification
        put("irons_spellbooks:volt_strike", SpellRange.CAC);
        put("irons_spellbooks:shockwave", SpellRange.CAC);
        put("irons_spellbooks:thunder_step", SpellRange.CAC);
        put("irons_spellbooks:ball_lightning", SpellRange.DISTANCE);
        put("irons_spellbooks:electrocute", SpellRange.DISTANCE);
        put("irons_spellbooks:chain_lightning", SpellRange.DISTANCE);
        put("irons_spellbooks:lightning_lance", SpellRange.DISTANCE);
        put("irons_spellbooks:thunderstorm", SpellRange.DISTANCE);
        put("irons_spellbooks:lightning_bolt", SpellRange.DISTANCE);
        put("irons_spellbooks:ascension", SpellRange.SELF);
        put("irons_spellbooks:charge", SpellRange.SELF);
    }

    private static void put(String id, SpellRange range) {
        SPELL_RANGES.put(id, range);
    }

    public static SpellRange of(AbstractSpell spell) {
        return SPELL_RANGES.getOrDefault(spell.getSpellId(), SpellRange.DISTANCE);
    }

    private SpellRangeRegistry() {}
}
