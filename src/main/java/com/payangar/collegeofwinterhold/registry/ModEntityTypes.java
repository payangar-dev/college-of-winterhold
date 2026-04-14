package com.payangar.collegeofwinterhold.registry;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureNoviceEntity;
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

    public static final DeferredHolder<EntityType<?>, EntityType<FireNoviceEntity>> FIRE_NOVICE =
            ENTITIES.register("fire_novice", () -> EntityType.Builder
                    .of(FireNoviceEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "fire_novice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FireApprenticeEntity>> FIRE_APPRENTICE =
            ENTITIES.register("fire_apprentice", () -> EntityType.Builder
                    .of(FireApprenticeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "fire_apprentice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FireAdeptEntity>> FIRE_ADEPT =
            ENTITIES.register("fire_adept", () -> EntityType.Builder
                    .of(FireAdeptEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "fire_adept").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FireExpertEntity>> FIRE_EXPERT =
            ENTITIES.register("fire_expert", () -> EntityType.Builder
                    .of(FireExpertEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "fire_expert").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FireMasterEntity>> FIRE_MASTER =
            ENTITIES.register("fire_master", () -> EntityType.Builder
                    .of(FireMasterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "fire_master").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<IceNoviceEntity>> ICE_NOVICE =
            ENTITIES.register("ice_novice", () -> EntityType.Builder
                    .of(IceNoviceEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ice_novice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<IceApprenticeEntity>> ICE_APPRENTICE =
            ENTITIES.register("ice_apprentice", () -> EntityType.Builder
                    .of(IceApprenticeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ice_apprentice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<IceAdeptEntity>> ICE_ADEPT =
            ENTITIES.register("ice_adept", () -> EntityType.Builder
                    .of(IceAdeptEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ice_adept").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<IceExpertEntity>> ICE_EXPERT =
            ENTITIES.register("ice_expert", () -> EntityType.Builder
                    .of(IceExpertEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ice_expert").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<IceMasterEntity>> ICE_MASTER =
            ENTITIES.register("ice_master", () -> EntityType.Builder
                    .of(IceMasterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ice_master").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderNoviceEntity>> ENDER_NOVICE =
            ENTITIES.register("ender_novice", () -> EntityType.Builder
                    .of(EnderNoviceEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ender_novice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderApprenticeEntity>> ENDER_APPRENTICE =
            ENTITIES.register("ender_apprentice", () -> EntityType.Builder
                    .of(EnderApprenticeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ender_apprentice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderAdeptEntity>> ENDER_ADEPT =
            ENTITIES.register("ender_adept", () -> EntityType.Builder
                    .of(EnderAdeptEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ender_adept").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderExpertEntity>> ENDER_EXPERT =
            ENTITIES.register("ender_expert", () -> EntityType.Builder
                    .of(EnderExpertEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ender_expert").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderMasterEntity>> ENDER_MASTER =
            ENTITIES.register("ender_master", () -> EntityType.Builder
                    .of(EnderMasterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "ender_master").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<NatureNoviceEntity>> NATURE_NOVICE =
            ENTITIES.register("nature_novice", () -> EntityType.Builder
                    .of(NatureNoviceEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "nature_novice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<NatureApprenticeEntity>> NATURE_APPRENTICE =
            ENTITIES.register("nature_apprentice", () -> EntityType.Builder
                    .of(NatureApprenticeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "nature_apprentice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<NatureAdeptEntity>> NATURE_ADEPT =
            ENTITIES.register("nature_adept", () -> EntityType.Builder
                    .of(NatureAdeptEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "nature_adept").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<NatureExpertEntity>> NATURE_EXPERT =
            ENTITIES.register("nature_expert", () -> EntityType.Builder
                    .of(NatureExpertEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "nature_expert").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<NatureMasterEntity>> NATURE_MASTER =
            ENTITIES.register("nature_master", () -> EntityType.Builder
                    .of(NatureMasterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "nature_master").toString()));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    private ModEntityTypes() {}
}
