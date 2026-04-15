package com.payangar.collegeofwinterhold.registry;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.vampire.VampireEntity;
import com.payangar.collegeofwinterhold.entity.vampire.VampireHoundEntity;
import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import com.payangar.collegeofwinterhold.entity.wizard.core.CollegeSchool;
import com.payangar.collegeofwinterhold.entity.wizard.core.WizardTier;
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
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyNoviceEntity;
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

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

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

    public static final DeferredHolder<EntityType<?>, EntityType<HolyNoviceEntity>> HOLY_NOVICE =
            ENTITIES.register("holy_novice", () -> EntityType.Builder
                    .of(HolyNoviceEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "holy_novice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HolyApprenticeEntity>> HOLY_APPRENTICE =
            ENTITIES.register("holy_apprentice", () -> EntityType.Builder
                    .of(HolyApprenticeEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "holy_apprentice").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HolyAdeptEntity>> HOLY_ADEPT =
            ENTITIES.register("holy_adept", () -> EntityType.Builder
                    .of(HolyAdeptEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "holy_adept").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HolyExpertEntity>> HOLY_EXPERT =
            ENTITIES.register("holy_expert", () -> EntityType.Builder
                    .of(HolyExpertEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "holy_expert").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HolyMasterEntity>> HOLY_MASTER =
            ENTITIES.register("holy_master", () -> EntityType.Builder
                    .of(HolyMasterEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "holy_master").toString()));

    // ── Vampires ─────────────────────────────────────────────────────────────
    public static final DeferredHolder<EntityType<?>, EntityType<VampireEntity>> VAMPIRE =
            ENTITIES.register("vampire", () -> EntityType.Builder
                    .of(VampireEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "vampire").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VampireHoundEntity>> VAMPIRE_HOUND =
            ENTITIES.register("vampire_hound", () -> EntityType.Builder
                    .of(VampireHoundEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.85f)
                    .clientTrackingRange(48)
                    .build(ResourceLocation.fromNamespaceAndPath(CollegeOfWinterhold.MODID, "vampire_hound").toString()));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    /**
     * (school, tier) → concrete wizard entity type lookup, used by the village
     * wizard spawner. Built lazily on first call because {@link DeferredHolder}
     * entries only resolve after the registry freeze; an eager static block
     * would give {@code null} suppliers. Returns {@code null} for combinations
     * that are not registered yet.
     */
    private static volatile Map<CollegeSchool, EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>>> WIZARD_LOOKUP;

    @Nullable
    public static EntityType<? extends AbstractCollegeWizardEntity> wizardFor(CollegeSchool school, WizardTier tier) {
        Map<CollegeSchool, EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>>> lookup = WIZARD_LOOKUP;
        if (lookup == null) {
            synchronized (ModEntityTypes.class) {
                lookup = WIZARD_LOOKUP;
                if (lookup == null) {
                    lookup = buildWizardLookup();
                    WIZARD_LOOKUP = lookup;
                }
            }
        }
        EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>> perSchool = lookup.get(school);
        if (perSchool == null) return null;
        DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>> holder = perSchool.get(tier);
        if (holder == null || !holder.isBound()) return null;
        return holder.get();
    }

    private static Map<CollegeSchool, EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>>> buildWizardLookup() {
        EnumMap<CollegeSchool, EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>>> map = new EnumMap<>(CollegeSchool.class);

        map.put(CollegeSchool.LIGHTNING, tierMap(LIGHTNING_NOVICE, LIGHTNING_APPRENTICE, LIGHTNING_ADEPT, LIGHTNING_EXPERT, LIGHTNING_MASTER));
        map.put(CollegeSchool.FIRE,      tierMap(FIRE_NOVICE,      FIRE_APPRENTICE,      FIRE_ADEPT,      FIRE_EXPERT,      FIRE_MASTER));
        map.put(CollegeSchool.ICE,       tierMap(ICE_NOVICE,       ICE_APPRENTICE,       ICE_ADEPT,       ICE_EXPERT,       ICE_MASTER));
        map.put(CollegeSchool.ENDER,     tierMap(ENDER_NOVICE,     ENDER_APPRENTICE,     ENDER_ADEPT,     ENDER_EXPERT,     ENDER_MASTER));
        map.put(CollegeSchool.NATURE,    tierMap(NATURE_NOVICE,    NATURE_APPRENTICE,    NATURE_ADEPT,    NATURE_EXPERT,    NATURE_MASTER));
        map.put(CollegeSchool.HOLY,      tierMap(HOLY_NOVICE,      HOLY_APPRENTICE,      HOLY_ADEPT,      HOLY_EXPERT,      HOLY_MASTER));

        return map;
    }

    private static EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>> tierMap(
            DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>> novice,
            DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>> apprentice,
            DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>> adept,
            DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>> expert,
            DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>> master) {
        EnumMap<WizardTier, DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractCollegeWizardEntity>>> m = new EnumMap<>(WizardTier.class);
        m.put(WizardTier.NOVICE, novice);
        m.put(WizardTier.APPRENTICE, apprentice);
        m.put(WizardTier.ADEPT, adept);
        m.put(WizardTier.EXPERT, expert);
        m.put(WizardTier.MASTER, master);
        return m;
    }

    private ModEntityTypes() {}
}
