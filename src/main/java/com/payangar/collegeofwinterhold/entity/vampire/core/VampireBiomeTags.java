package com.payangar.collegeofwinterhold.entity.vampire.core;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class VampireBiomeTags {
    public static final TagKey<Biome> VAMPIRE_COVEN_BIOMES = create("vampire_coven_biomes");

    private static TagKey<Biome> create(String path) {
        return TagKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, path));
    }

    private VampireBiomeTags() {}
}
