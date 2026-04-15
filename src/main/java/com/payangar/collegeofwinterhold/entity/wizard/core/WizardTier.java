package com.payangar.collegeofwinterhold.entity.wizard.core;

import com.payangar.collegeofwinterhold.entity.wizard.CollegeWizardEquipment;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public enum WizardTier {
    NOVICE(
            2, SpellRarity.COMMON,    1, 1,
            0, SpellRarity.COMMON,    1, 1,
            5,  5,   20.0, 1.0, 0.25, 16.0,
            ItemRegistry.COPPER_SPELL_BOOK),
    APPRENTICE(
            4, SpellRarity.UNCOMMON,  2, 3,
            0, SpellRarity.COMMON,    1, 1,
            5,  8,   30.0, 2.0, 0.26, 20.0,
            ItemRegistry.COPPER_SPELL_BOOK),
    ADEPT(
            5, SpellRarity.RARE,      4, 5,
            1, SpellRarity.COMMON,    1, 1,
            6, 12,   45.0, 3.0, 0.27, 24.0,
            ItemRegistry.IRON_SPELL_BOOK),
    EXPERT(
            6, SpellRarity.EPIC,      6, 7,
            2, SpellRarity.UNCOMMON,  2, 3,
            8, 18,   65.0, 4.0, 0.28, 28.0,
            ItemRegistry.GOLD_SPELL_BOOK),
    MASTER(
            8, SpellRarity.LEGENDARY, 8, 9,
            4, SpellRarity.RARE,      4, 5,
            10, 25,  90.0, 5.0, 0.29, 32.0,
            ItemRegistry.DIAMOND_SPELL_BOOK);

    private final int knownNativeCount;
    private final SpellRarity nativeMaxRarity;
    private final int nativeLevelMin;
    private final int nativeLevelMax;

    private final int knownForeignCount;
    private final SpellRarity foreignMaxRarity;
    private final int foreignLevelMin;
    private final int foreignLevelMax;

    private final int bookSlots;
    private final int xpReward;

    private final double maxHealth;
    private final double attackDamage;
    private final double movementSpeed;
    private final double followRange;

    private final Supplier<Item> spellbookItem;

    WizardTier(
            int knownNativeCount, SpellRarity nativeMaxRarity, int nativeLevelMin, int nativeLevelMax,
            int knownForeignCount, SpellRarity foreignMaxRarity, int foreignLevelMin, int foreignLevelMax,
            int bookSlots, int xpReward,
            double maxHealth, double attackDamage, double movementSpeed, double followRange,
            Supplier<Item> spellbookItem) {
        this.knownNativeCount = knownNativeCount;
        this.nativeMaxRarity = nativeMaxRarity;
        this.nativeLevelMin = nativeLevelMin;
        this.nativeLevelMax = nativeLevelMax;
        this.knownForeignCount = knownForeignCount;
        this.foreignMaxRarity = foreignMaxRarity;
        this.foreignLevelMin = foreignLevelMin;
        this.foreignLevelMax = foreignLevelMax;
        this.bookSlots = bookSlots;
        this.xpReward = xpReward;
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.movementSpeed = movementSpeed;
        this.followRange = followRange;
        this.spellbookItem = spellbookItem;
    }

    public int knownNativeCount()     { return knownNativeCount; }
    public SpellRarity nativeMaxRarity() { return nativeMaxRarity; }
    public int nativeLevelMin()       { return nativeLevelMin; }
    public int nativeLevelMax()       { return nativeLevelMax; }

    public int knownForeignCount()    { return knownForeignCount; }
    public SpellRarity foreignMaxRarity() { return foreignMaxRarity; }
    public int foreignLevelMin()      { return foreignLevelMin; }
    public int foreignLevelMax()      { return foreignLevelMax; }

    public int bookSlots()            { return bookSlots; }
    public int xpReward()             { return xpReward; }

    public double maxHealth()         { return maxHealth; }
    public double attackDamage()      { return attackDamage; }
    public double movementSpeed()     { return movementSpeed; }
    public double followRange()       { return followRange; }

    public Supplier<Item> spellbookItem() { return spellbookItem; }

    public void applyArmor(Mob mob, RandomSource rng, CollegeWizardEquipment.SchoolArmorSet schoolSet) {
        switch (this) {
            case NOVICE -> { /* no armor */ }
            case APPRENTICE -> CollegeWizardEquipment.applyApprenticeArmor(mob, rng);
            case ADEPT      -> CollegeWizardEquipment.applyAdeptArmor(mob, rng);
            case EXPERT     -> CollegeWizardEquipment.applyExpertArmor(mob, rng, schoolSet);
            case MASTER     -> CollegeWizardEquipment.applyMasterArmor(mob, rng, schoolSet);
        }
    }

    /**
     * Rolls a tier in the Adept → Master range according to the village spawn
     * distribution : 70% Adept, 25% Expert, 5% Master. Used by the village
     * wizard spawner so Novice & Apprentice stay reserved for the future
     * custom "College of Winterhold" school structure.
     */
    public static WizardTier rollAdeptPlus(RandomSource rng) {
        float r = rng.nextFloat();
        if (r < 0.70f) return ADEPT;
        if (r < 0.95f) return EXPERT;
        return MASTER;
    }
}
