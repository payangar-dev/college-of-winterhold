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

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private ModItems() {}
}
