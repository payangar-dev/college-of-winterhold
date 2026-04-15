package com.payangar.collegeofwinterhold.entity.vampire.core;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Random spawn variant for a vampire. The carried spellbook acts as the de-facto
 * power tier: no book → weak, Iron book → mid, Vampiric book → strong,
 * Necronomicon → elite. Each variant carries its own HP range, spell count,
 * level range, rarity cap and armor piece count. Drawn via
 * {@link #rollVariant(RandomSource)} at spawn time.
 *
 * <p>Probabilities sum to 1.0 in declaration order ({@code NO_BOOK 40 / IRON 30
 * / VAMPIRIC 20 / NECRONOMICON 10}).
 *
 * <p>Special case — Necronomicon: its {@link #spellbookItem()} is
 * {@link ItemRegistry#NECRONOMICON} which is a {@code UniqueSpellBook} pre-filled
 * with 4 canonical level-5 spells (Blood Slash, Blood Step, Ray of Siphoning,
 * Blaze Storm). The vampire entity keeps those 4 canonical spells and adds
 * {@link #spellCountMin} extra rolled Blood spells on top — see the concrete
 * vampire's {@code rollLoadout} for the merge logic.
 */
public enum VampireVariant {
    NO_BOOK(
            0.40f,
            18, 24,
            3, 3,
            1, 2,
            SpellRarity.COMMON,
            0, 1,
            5,
            null,
            ItemRegistry.INK_COMMON),
    IRON_BOOK(
            0.30f,
            22, 28,
            4, 5,
            2, 3,
            SpellRarity.UNCOMMON,
            1, 2,
            8,
            ItemRegistry.IRON_SPELL_BOOK,
            ItemRegistry.INK_UNCOMMON),
    VAMPIRIC_BOOK(
            0.20f,
            26, 32,
            7, 8,
            3, 5,
            SpellRarity.RARE,
            2, 3,
            12,
            ItemRegistry.CURSED_DOLL_SPELLBOOK,
            ItemRegistry.INK_RARE),
    NECRONOMICON(
            0.10f,
            30, 36,
            2, 2,    // 2 rolled spells on top of the 4 canonical ones = 6 total
            5, 7,
            SpellRarity.EPIC,
            3, 4,
            18,
            ItemRegistry.NECRONOMICON,
            ItemRegistry.INK_EPIC);

    private final float probability;
    private final int healthMin;
    private final int healthMax;
    private final int spellCountMin;
    private final int spellCountMax;
    private final int spellLevelMin;
    private final int spellLevelMax;
    private final SpellRarity maxRarity;
    private final int armorPiecesMin;
    private final int armorPiecesMax;
    private final int xpReward;
    @Nullable
    private final Supplier<Item> spellbookItem;
    private final Supplier<Item> inkItem;

    VampireVariant(float probability,
                   int healthMin, int healthMax,
                   int spellCountMin, int spellCountMax,
                   int spellLevelMin, int spellLevelMax,
                   SpellRarity maxRarity,
                   int armorPiecesMin, int armorPiecesMax,
                   int xpReward,
                   @Nullable Supplier<Item> spellbookItem,
                   Supplier<Item> inkItem) {
        this.probability = probability;
        this.healthMin = healthMin;
        this.healthMax = healthMax;
        this.spellCountMin = spellCountMin;
        this.spellCountMax = spellCountMax;
        this.spellLevelMin = spellLevelMin;
        this.spellLevelMax = spellLevelMax;
        this.maxRarity = maxRarity;
        this.armorPiecesMin = armorPiecesMin;
        this.armorPiecesMax = armorPiecesMax;
        this.xpReward = xpReward;
        this.spellbookItem = spellbookItem;
        this.inkItem = inkItem;
    }

    public float probability()       { return probability; }
    public int healthMin()            { return healthMin; }
    public int healthMax()            { return healthMax; }
    public int spellCountMin()        { return spellCountMin; }
    public int spellCountMax()        { return spellCountMax; }
    public int spellLevelMin()        { return spellLevelMin; }
    public int spellLevelMax()        { return spellLevelMax; }
    public SpellRarity maxRarity()    { return maxRarity; }
    public int armorPiecesMin()       { return armorPiecesMin; }
    public int armorPiecesMax()       { return armorPiecesMax; }
    public int xpReward()             { return xpReward; }
    @Nullable public Supplier<Item> spellbookItem() { return spellbookItem; }
    public Supplier<Item> inkItem()                 { return inkItem; }

    public boolean hasBook() { return spellbookItem != null; }

    /** Rolls an integer in {@code [min, max]} (inclusive). */
    public int rollHealth(RandomSource rng) {
        return healthMin + rng.nextInt(healthMax - healthMin + 1);
    }

    public int rollSpellCount(RandomSource rng) {
        return spellCountMin + rng.nextInt(spellCountMax - spellCountMin + 1);
    }

    public int rollArmorPieces(RandomSource rng) {
        return armorPiecesMin + rng.nextInt(armorPiecesMax - armorPiecesMin + 1);
    }

    /**
     * Rolls a variant according to the declared probability distribution.
     * Iterates in enum-declaration order and falls through to the last variant
     * if floating-point drift leaves no match.
     */
    public static VampireVariant rollVariant(RandomSource rng) {
        float r = rng.nextFloat();
        float accumulated = 0f;
        VampireVariant[] values = values();
        for (VampireVariant v : values) {
            accumulated += v.probability;
            if (r < accumulated) return v;
        }
        return values[values.length - 1];
    }
}
