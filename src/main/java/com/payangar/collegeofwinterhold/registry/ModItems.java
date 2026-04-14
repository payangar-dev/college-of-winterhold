package com.payangar.collegeofwinterhold.registry;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CollegeOfWinterhold.MODID);

    public static final DeferredItem<Item> LIGHTNING_NOVICE_SPAWN_EGG =
            ITEMS.register("lightning_novice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.LIGHTNING_NOVICE,
                    0x0B1033,
                    0xF5D742,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> LIGHTNING_APPRENTICE_SPAWN_EGG =
            ITEMS.register("lightning_apprentice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.LIGHTNING_APPRENTICE,
                    0x0B1033,
                    0xF5D742,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> LIGHTNING_ADEPT_SPAWN_EGG =
            ITEMS.register("lightning_adept_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.LIGHTNING_ADEPT,
                    0x0B1033,
                    0xF5D742,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> LIGHTNING_EXPERT_SPAWN_EGG =
            ITEMS.register("lightning_expert_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.LIGHTNING_EXPERT,
                    0x0B1033,
                    0xF5D742,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> LIGHTNING_MASTER_SPAWN_EGG =
            ITEMS.register("lightning_master_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.LIGHTNING_MASTER,
                    0x0B1033,
                    0xF5D742,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> FIRE_NOVICE_SPAWN_EGG =
            ITEMS.register("fire_novice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.FIRE_NOVICE,
                    0x7A1A0A,
                    0xF59E2C,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> FIRE_APPRENTICE_SPAWN_EGG =
            ITEMS.register("fire_apprentice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.FIRE_APPRENTICE,
                    0x7A1A0A,
                    0xF59E2C,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> FIRE_ADEPT_SPAWN_EGG =
            ITEMS.register("fire_adept_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.FIRE_ADEPT,
                    0x7A1A0A,
                    0xF59E2C,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> FIRE_EXPERT_SPAWN_EGG =
            ITEMS.register("fire_expert_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.FIRE_EXPERT,
                    0x7A1A0A,
                    0xF59E2C,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> FIRE_MASTER_SPAWN_EGG =
            ITEMS.register("fire_master_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.FIRE_MASTER,
                    0x7A1A0A,
                    0xF59E2C,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ICE_NOVICE_SPAWN_EGG =
            ITEMS.register("ice_novice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ICE_NOVICE,
                    0x8EC5E8,
                    0xFFFFFF,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ICE_APPRENTICE_SPAWN_EGG =
            ITEMS.register("ice_apprentice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ICE_APPRENTICE,
                    0x8EC5E8,
                    0xFFFFFF,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ICE_ADEPT_SPAWN_EGG =
            ITEMS.register("ice_adept_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ICE_ADEPT,
                    0x8EC5E8,
                    0xFFFFFF,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ICE_EXPERT_SPAWN_EGG =
            ITEMS.register("ice_expert_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ICE_EXPERT,
                    0x8EC5E8,
                    0xFFFFFF,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ICE_MASTER_SPAWN_EGG =
            ITEMS.register("ice_master_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ICE_MASTER,
                    0x8EC5E8,
                    0xFFFFFF,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ENDER_NOVICE_SPAWN_EGG =
            ITEMS.register("ender_novice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ENDER_NOVICE,
                    0x1A0B2E,
                    0xB044E6,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ENDER_APPRENTICE_SPAWN_EGG =
            ITEMS.register("ender_apprentice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ENDER_APPRENTICE,
                    0x1A0B2E,
                    0xB044E6,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ENDER_ADEPT_SPAWN_EGG =
            ITEMS.register("ender_adept_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ENDER_ADEPT,
                    0x1A0B2E,
                    0xB044E6,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ENDER_EXPERT_SPAWN_EGG =
            ITEMS.register("ender_expert_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ENDER_EXPERT,
                    0x1A0B2E,
                    0xB044E6,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> ENDER_MASTER_SPAWN_EGG =
            ITEMS.register("ender_master_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.ENDER_MASTER,
                    0x1A0B2E,
                    0xB044E6,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> NATURE_NOVICE_SPAWN_EGG =
            ITEMS.register("nature_novice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.NATURE_NOVICE,
                    0x2D5A2D,
                    0x8BC34A,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> NATURE_APPRENTICE_SPAWN_EGG =
            ITEMS.register("nature_apprentice_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.NATURE_APPRENTICE,
                    0x2D5A2D,
                    0x8BC34A,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> NATURE_ADEPT_SPAWN_EGG =
            ITEMS.register("nature_adept_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.NATURE_ADEPT,
                    0x2D5A2D,
                    0x8BC34A,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> NATURE_EXPERT_SPAWN_EGG =
            ITEMS.register("nature_expert_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.NATURE_EXPERT,
                    0x2D5A2D,
                    0x8BC34A,
                    new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> NATURE_MASTER_SPAWN_EGG =
            ITEMS.register("nature_master_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntityTypes.NATURE_MASTER,
                    0x2D5A2D,
                    0x8BC34A,
                    new Item.Properties().stacksTo(64)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
