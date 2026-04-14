package com.payangar.collegeofwinterhold.registry;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningNoviceEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CollegeOfWinterhold.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<LightningNoviceEntity>> LIGHTNING_NOVICE =
            ENTITIES.register("lightning_novice", () -> EntityType.Builder
                    .of(LightningNoviceEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "lightning_novice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningApprenticeEntity>> LIGHTNING_APPRENTICE =
            ENTITIES.register("lightning_apprentice", () -> EntityType.Builder
                    .of(LightningApprenticeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "lightning_apprentice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningAdeptEntity>> LIGHTNING_ADEPT =
            ENTITIES.register("lightning_adept", () -> EntityType.Builder
                    .of(LightningAdeptEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "lightning_adept").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningExpertEntity>> LIGHTNING_EXPERT =
            ENTITIES.register("lightning_expert", () -> EntityType.Builder
                    .of(LightningExpertEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "lightning_expert").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningMasterEntity>> LIGHTNING_MASTER =
            ENTITIES.register("lightning_master", () -> EntityType.Builder
                    .of(LightningMasterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "lightning_master").toString()));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    private ModEntityTypes() {}
}
