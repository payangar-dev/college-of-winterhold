package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.payangar.collegeofwinterhold.entity.ai.SpellCategory.*;
import static com.payangar.collegeofwinterhold.entity.ai.SpellTargeting.*;

public final class SpellClassificationRegistry {
    private static final Map<String, SpellClassification> CLASSIFICATIONS = new HashMap<>();

    private static final SpellClassification DEFAULT =
            new SpellClassification(EnumSet.of(ATTACK_DISTANCE), TARGET);

    static {
        // ── Lightning ─────────────────────────────────────────────────────────
        put("irons_spellbooks:volt_strike",      TARGET, ATTACK_CAC);
        put("irons_spellbooks:shockwave",        AOE,    ATTACK_CAC);
        put("irons_spellbooks:ball_lightning",   TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:electrocute",      TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:chain_lightning",  AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:lightning_lance",  TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:thunder_step",     TARGET, ATTACK_DISTANCE, MOVEMENT);
        put("irons_spellbooks:thunderstorm",     AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:lightning_bolt",   TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:ascension",        SELF,   BUFF);
        put("irons_spellbooks:charge",           SELF,   BUFF);

        // ── Holy ──────────────────────────────────────────────────────────────
        put("irons_spellbooks:divine_smite",          TARGET, ATTACK_CAC);
        put("irons_spellbooks:guiding_bolt",          TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:wisp",                  SELF,   BUFF);
        put("irons_spellbooks:fortify",               SELF,   BUFF);
        put("irons_spellbooks:blessing_of_life",      SELF,   HEAL, BUFF);
        put("irons_spellbooks:healing_circle",        AOE,    HEAL);
        put("irons_spellbooks:cloud_of_regeneration", AOE,    HEAL);
        put("irons_spellbooks:heal",                  SELF,   HEAL);
        put("irons_spellbooks:sunbeam",               TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:greater_heal",          SELF,   HEAL);
        put("irons_spellbooks:cleanse",               SELF,   DEFENSE);
        put("irons_spellbooks:haste",                 SELF,   BUFF, MOVEMENT);
        put("irons_spellbooks:angel_wing",            SELF,   BUFF, MOVEMENT);

        // ── Fire ──────────────────────────────────────────────────────────────
        put("irons_spellbooks:firebolt",         TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:fire_breath",      AOE,    ATTACK_CAC);
        put("irons_spellbooks:flaming_strike",   TARGET, ATTACK_CAC);
        put("irons_spellbooks:burning_dash",     AOE,    ATTACK_DISTANCE, MOVEMENT);
        put("irons_spellbooks:wall_of_fire",     AOE,    ATTACK_DISTANCE, DEFENSE);
        put("irons_spellbooks:heat_surge",       AOE,    ATTACK_CAC);
        put("irons_spellbooks:blaze_storm",      AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:magma_bomb",       AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:scorch",           AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:fire_arrow",       TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:fireball",         AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:flaming_barrage",  AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:raise_hell",       AOE,    ATTACK_CAC);

        // ── Ice ───────────────────────────────────────────────────────────────
        put("irons_spellbooks:cone_of_cold",        AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:frostwave",           AOE,    ATTACK_CAC);
        put("irons_spellbooks:ice_spikes",          AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:icicle",              TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:ray_of_frost",        TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:ice_tomb",            TARGET, ATTACK_DISTANCE, DEFENSE);
        put("irons_spellbooks:snowball",            AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:frost_step",          TARGET, ATTACK_DISTANCE, MOVEMENT);
        put("irons_spellbooks:ice_block",           SELF,   DEFENSE);
        put("irons_spellbooks:summon_polar_bear",   SELF,   BUFF);
        put("irons_spellbooks:frostbite",           AOE,    ATTACK_DISTANCE);

        // ── Nature ────────────────────────────────────────────────────────────
        // Excluded: touch_dig (mining utility, no combat use)
        put("irons_spellbooks:acid_orb",       AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:gluttony",       SELF,   BUFF);
        put("irons_spellbooks:oakskin",        SELF,   BUFF);
        put("irons_spellbooks:poison_arrow",   TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:poison_breath",  AOE,    ATTACK_CAC);
        put("irons_spellbooks:earthquake",     AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:firefly_swarm",  AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:poison_splash",  AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:root",           TARGET, ATTACK_DISTANCE, DEFENSE);
        put("irons_spellbooks:stomp",          AOE,    ATTACK_CAC);
        put("irons_spellbooks:blight",         TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:spider_aspect",  SELF,   BUFF);

        // ── Ender ─────────────────────────────────────────────────────────────
        // Excluded: recall, portal, summon_ender_chest (utility, no combat use)
        put("irons_spellbooks:dragon_breath",    AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:magic_missile",    TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:shadow_slash",     TARGET, ATTACK_CAC);
        put("irons_spellbooks:starfall",         AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:teleport",         SELF,   MOVEMENT);
        put("irons_spellbooks:counterspell",     TARGET, DEFENSE);
        put("irons_spellbooks:echoing_strikes",  SELF,   BUFF);
        put("irons_spellbooks:magic_arrow",      TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:summon_swords",    SELF,   BUFF);
        put("irons_spellbooks:evasion",          SELF,   BUFF);
        put("irons_spellbooks:black_hole",       AOE,    ATTACK_DISTANCE);

        // ── Blood ─────────────────────────────────────────────────────────────
        // Excluded: sacrifice (requires friendly-target data the mob AI cannot supply)
        put("irons_spellbooks:acupuncture",      TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:blood_needles",    AOE,    ATTACK_DISTANCE);
        put("irons_spellbooks:blood_slash",      TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:blood_step",       TARGET, ATTACK_DISTANCE, MOVEMENT);
        put("irons_spellbooks:devour",           TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:heartstop",        SELF,   BUFF);
        put("irons_spellbooks:raise_dead",       SELF,   BUFF);
        put("irons_spellbooks:ray_of_siphoning", TARGET, ATTACK_DISTANCE);
        put("irons_spellbooks:wither_skull",     TARGET, ATTACK_DISTANCE);
    }

    private static void put(String id, SpellTargeting targeting, SpellCategory... categories) {
        Set<SpellCategory> set = EnumSet.noneOf(SpellCategory.class);
        for (SpellCategory c : categories) set.add(c);
        CLASSIFICATIONS.put(id, new SpellClassification(set, targeting));
    }

    public static SpellClassification of(AbstractSpell spell) {
        return CLASSIFICATIONS.getOrDefault(spell.getSpellId(), DEFAULT);
    }

    /**
     * Whitelist check : a spell is part of the College pool iff it has an explicit
     * classification entry. Utility spells like {@code touch_dig}, {@code recall},
     * {@code portal} and {@code summon_ender_chest} are intentionally absent and
     * will not be rolled by any wizard loadout.
     */
    public static boolean hasEntry(AbstractSpell spell) {
        return CLASSIFICATIONS.containsKey(spell.getSpellId());
    }

    private SpellClassificationRegistry() {}
}
