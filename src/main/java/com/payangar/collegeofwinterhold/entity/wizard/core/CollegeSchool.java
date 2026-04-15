package com.payangar.collegeofwinterhold.entity.wizard.core;

import com.payangar.collegeofwinterhold.entity.ai.SchoolTendency;
import com.payangar.collegeofwinterhold.entity.wizard.CollegeWizardEquipment.SchoolArmorSet;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Supplier;

public enum CollegeSchool {
    FIRE(
            SchoolRegistry.FIRE,
            SchoolTendency.FIRE,
            new SchoolArmorSet(
                    ItemRegistry.PYROMANCER_HELMET,
                    ItemRegistry.PYROMANCER_CHESTPLATE,
                    ItemRegistry.PYROMANCER_LEGGINGS,
                    ItemRegistry.PYROMANCER_BOOTS)),
    ICE(
            SchoolRegistry.ICE,
            SchoolTendency.ICE,
            new SchoolArmorSet(
                    ItemRegistry.CRYOMANCER_HELMET,
                    ItemRegistry.CRYOMANCER_CHESTPLATE,
                    ItemRegistry.CRYOMANCER_LEGGINGS,
                    ItemRegistry.CRYOMANCER_BOOTS)),
    LIGHTNING(
            SchoolRegistry.LIGHTNING,
            SchoolTendency.LIGHTNING,
            new SchoolArmorSet(
                    ItemRegistry.ELECTROMANCER_HELMET,
                    ItemRegistry.ELECTROMANCER_CHESTPLATE,
                    ItemRegistry.ELECTROMANCER_LEGGINGS,
                    ItemRegistry.ELECTROMANCER_BOOTS)),
    ENDER(
            SchoolRegistry.ENDER,
            SchoolTendency.ENDER,
            new SchoolArmorSet(
                    ItemRegistry.SHADOWWALKER_HELMET,
                    ItemRegistry.SHADOWWALKER_CHESTPLATE,
                    ItemRegistry.SHADOWWALKER_LEGGINGS,
                    ItemRegistry.SHADOWWALKER_BOOTS)),
    NATURE(
            SchoolRegistry.NATURE,
            SchoolTendency.NATURE,
            new SchoolArmorSet(
                    ItemRegistry.PLAGUED_HELMET,
                    ItemRegistry.PLAGUED_CHESTPLATE,
                    ItemRegistry.PLAGUED_LEGGINGS,
                    ItemRegistry.PLAGUED_BOOTS)),
    HOLY(
            SchoolRegistry.HOLY,
            SchoolTendency.HOLY,
            new SchoolArmorSet(
                    ItemRegistry.PRIEST_HELMET,
                    ItemRegistry.PRIEST_CHESTPLATE,
                    ItemRegistry.PRIEST_LEGGINGS,
                    ItemRegistry.PRIEST_BOOTS));

    private final Supplier<SchoolType> ironsSchool;
    private final SchoolTendency tendency;
    private final SchoolArmorSet armorSet;

    CollegeSchool(Supplier<SchoolType> ironsSchool, SchoolTendency tendency, SchoolArmorSet armorSet) {
        this.ironsSchool = ironsSchool;
        this.tendency = tendency;
        this.armorSet = armorSet;
    }

    public Supplier<SchoolType> ironsSchool() { return ironsSchool; }
    public SchoolTendency tendency()          { return tendency; }
    public SchoolArmorSet armorSet()          { return armorSet; }

    /**
     * Resolves the school that spawns in a given biome, in priority order :
     * Nature > Ender > Ice > Fire > Lightning > Holy (fallback). Used at
     * village-generated wizard spawn time to pick the school from the biome
     * via custom {@link WizardBiomeTags}. Any biome that matches no tag falls
     * through to {@link #HOLY}, so this method never returns {@code null}.
     */
    public static CollegeSchool forBiome(Holder<Biome> biome) {
        if (biome.is(WizardBiomeTags.NATURE_WIZARD_BIOMES))    return NATURE;
        if (biome.is(WizardBiomeTags.ENDER_WIZARD_BIOMES))     return ENDER;
        if (biome.is(WizardBiomeTags.ICE_WIZARD_BIOMES))       return ICE;
        if (biome.is(WizardBiomeTags.FIRE_WIZARD_BIOMES))      return FIRE;
        if (biome.is(WizardBiomeTags.LIGHTNING_WIZARD_BIOMES)) return LIGHTNING;
        return HOLY;
    }
}
