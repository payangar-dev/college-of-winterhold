package com.payangar.collegeofwinterhold.entity.wizard.core;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class WizardBiomeTags {
    public static final TagKey<Biome> NATURE_WIZARD_BIOMES    = create("nature_wizard_biomes");
    public static final TagKey<Biome> ENDER_WIZARD_BIOMES     = create("ender_wizard_biomes");
    public static final TagKey<Biome> ICE_WIZARD_BIOMES       = create("ice_wizard_biomes");
    public static final TagKey<Biome> FIRE_WIZARD_BIOMES      = create("fire_wizard_biomes");
    public static final TagKey<Biome> LIGHTNING_WIZARD_BIOMES = create("lightning_wizard_biomes");
    public static final TagKey<Biome> HOLY_WIZARD_BIOMES      = create("holy_wizard_biomes");

    private static TagKey<Biome> create(String path) {
        return TagKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, path));
    }

    private WizardBiomeTags() {}
}
